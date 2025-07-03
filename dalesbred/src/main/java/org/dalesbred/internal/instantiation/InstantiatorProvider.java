/*
 * Copyright (c) 2018 Evident Solutions Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.dalesbred.internal.instantiation;

import org.dalesbred.annotation.DalesbredIgnore;
import org.dalesbred.annotation.DalesbredInstantiator;
import org.dalesbred.conversion.TypeConversionRegistry;
import org.dalesbred.dialect.Dialect;
import org.dalesbred.integration.joda.JodaTypeConversions;
import org.dalesbred.integration.threeten.ThreeTenTypeConversions;
import org.dalesbred.internal.utils.OptionalUtils;
import org.dalesbred.internal.utils.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.sql.Array;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isPublic;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.dalesbred.internal.utils.CollectionUtils.arrayOfType;
import static org.dalesbred.internal.utils.TypeUtils.*;

/**
 * Provides {@link Instantiator}s for classes.
 */
public final class InstantiatorProvider {

    private final @NotNull Dialect dialect;

    private final @NotNull DefaultTypeConversionRegistry typeConversionRegistry;

    private static final @NotNull Logger log = LoggerFactory.getLogger(InstantiatorProvider.class);

    public InstantiatorProvider(@NotNull Dialect dialect) {
        this.dialect = requireNonNull(dialect);
        this.typeConversionRegistry = new DefaultTypeConversionRegistry(dialect);

        DefaultTypeConversions.register(typeConversionRegistry);

        if (JodaTypeConversions.hasJoda()) {
            log.debug("Detected Joda Time in classpath. Registering type conversions for Joda.");
            JodaTypeConversions.register(typeConversionRegistry);
        }

        if (ThreeTenTypeConversions.hasThreeTen()) {
            log.debug("Detected ThreeTen in classpath. Registering type conversions for it.");
            ThreeTenTypeConversions.register(typeConversionRegistry);
        }
    }

    public @Nullable Object valueToDatabase(@Nullable Object value) {
        if (value == null) return null;

        TypeConversion conversion = typeConversionRegistry.findConversionToDb(value.getClass()).orElse(null);
        if (conversion != null)
            return conversion.convert(value);
        else if (value instanceof Enum<?>)
            return dialect.valueToDatabase(((Enum<?>) value).name());
        else
            return dialect.valueToDatabase(value);
    }

    @SuppressWarnings("unchecked")
    public @NotNull <T> Instantiator<T> findInstantiator(@NotNull Class<T> type, @NotNull NamedTypeList types) {
        return (Instantiator<T>) findInstantiator((Type) type, types);
    }

    public @NotNull Instantiator<?> findInstantiator(@NotNull Type type, @NotNull NamedTypeList types) {
        // First check if we have an immediate conversion registered. If so, we'll just use that.
        if (types.size() == 1) {
            TypeConversion conversion = findConversionFromDbValue(types.getType(0), type).orElse(null);
            if (conversion != null)
                return args -> conversion.convert(args.getSingleValue());
        }

        Class<?> cl = rawType(type);

        Instantiator<?> instantiator = findExplicitInstantiatorFor(cl, types);
        if (instantiator != null)
            return instantiator;

        if (!isPublic(cl.getModifiers()))
            throw new InstantiationFailureException(type + " can't be instantiated reflectively because it is not public or missing a @DalesbredInstantiator-annotation");

        return candidateConstructorsSortedByDescendingParameterCount(cl)
                .map(ctor -> implicitInstantiatorFrom(ctor, types).orElse(null))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new InstantiationFailureException("could not find a way to instantiate " + type + " with parameters " + types));
    }

    private @Nullable Instantiator<?> findExplicitInstantiatorFor(Class<?> cl, @NotNull NamedTypeList types) throws InstantiationFailureException {
        Executable ctorOrMethod = findExplicitInstantiatorReference(cl);

        if (ctorOrMethod == null)
            return null;

        try {
            ReflectionUtils.makeAccessible(ctorOrMethod);
        } catch (SecurityException e) {
            throw new InstantiationFailureException("Cannot instantiate " + cl.getName() +" using non-public constructor due to Security exception", e);
        }

        List<String> columnNames = types.getNames();
        List<Type> parameterTypes = asList(ctorOrMethod.getGenericParameterTypes());

        if (parameterTypes.size() != columnNames.size())
            throw new InstantiationFailureException(String.format("Cannot instantiate %s, constructor takes %d arguments, but result set has %d",
                    cl.getName(), parameterTypes.size(), columnNames.size()));

        ReflectionInstantiator<?> instantiator = resolveConversions(types, parameterTypes)
                .map(conversions -> new ReflectionInstantiator<>(ctorOrMethod, conversions, Collections.emptyList()))
                .orElseThrow(() -> new InstantiationFailureException("could not find a way to instantiate " + cl.getName() + " with parameters " + types));

        return instantiator;
    }

    /**
     * Returns an instantiator that uses given constructor and given types to create instances,
     * or empty if there are no conversions that can be made to instantiate the type.
     */
    private @NotNull <T> Optional<Instantiator<T>> implicitInstantiatorFrom(@NotNull Constructor<T> constructor, @NotNull NamedTypeList types) {
        if (!isPublic(constructor.getModifiers()))
            return Optional.empty();

        List<String> columnNames = types.getNames();
        return findTargetTypes(constructor, columnNames)
                .flatMap(targetTypes -> resolveConversions(types, targetTypes)
                        .map(conversions -> new ReflectionInstantiator<>(constructor, conversions, createPropertyAccessorsForValuesNotCoveredByConstructor(constructor, columnNames))));
    }

    private static @NotNull List<PropertyAccessor> createPropertyAccessorsForValuesNotCoveredByConstructor(@NotNull Constructor<?> constructor,
                                                                                                           @NotNull List<String> names) {
        int constructorParameterCount = constructor.getParameterTypes().length;
        int accessorCount = names.size() - constructorParameterCount;
        ArrayList<PropertyAccessor> accessors = new ArrayList<>(accessorCount);

        for (int i = 0; i < accessorCount; i++)
            accessors.add(createAccessor(i + constructorParameterCount, constructor.getDeclaringClass(), names));

        return accessors;
    }

    private static @NotNull PropertyAccessor createAccessor(int index, @NotNull Class<?> cl, @NotNull List<String> names) {
        return PropertyAccessor.findAccessor(cl, names.get(index)).orElseThrow(() ->
                new InstantiationFailureException("Could not find neither setter nor field for '" + names.get(index) + '\''));
    }

    /**
     * Returns the target types that need to have conversion. The types contain first as many constructor
     * parameter types as we have and then the types of properties of object as given by names of result-set.
     */
    private static @NotNull Optional<List<Type>> findTargetTypes(@NotNull Constructor<?> ctor, @NotNull List<String> resultSetColumns) {
        List<Type> constructorParameterTypes = asList(ctor.getGenericParameterTypes());

        int constructorParameterCount = constructorParameterTypes.size();

        if (constructorParameterCount > resultSetColumns.size()) {
            // We don't have enough columns in ResultSet to instantiate this constructor, discard it.
            return Optional.empty();

        } else if (constructorParameterCount == resultSetColumns.size()) {
            // We have exactly enough column in ResultSet. Use the constructor as it is.
            return Optional.of(constructorParameterTypes);

        } else {
            // Get the types of remaining properties
            ArrayList<Type> result = new ArrayList<>(resultSetColumns.size());
            result.addAll(constructorParameterTypes);

            List<String> propertyNames = resultSetColumns.subList(constructorParameterCount, resultSetColumns.size());
            for (String name : propertyNames) {
                Type type = PropertyAccessor.findPropertyType(ctor.getDeclaringClass(), name).orElse(null);
                if (type != null)
                    result.add(type);
                else
                    return Optional.empty();
            }

            return Optional.of(result);
        }
    }

    /**
     * Returns the list of conversions that need to be performed to convert sourceTypes
     * to targetTypes, or empty if conversions can't be done.
     */
    private @NotNull Optional<List<TypeConversion>> resolveConversions(@NotNull NamedTypeList sourceTypes, @NotNull List<Type> targetTypes) {
        if (targetTypes.size() != sourceTypes.size())
            return Optional.empty();

        ArrayList<TypeConversion> conversions = new ArrayList<>(targetTypes.size());

        for (int i = 0, len = targetTypes.size(); i < len; i++) {
            TypeConversion conversion = findConversionFromDbValue(sourceTypes.getType(i), targetTypes.get(i)).orElse(null);
            if (conversion != null)
                conversions.add(conversion);
            else
                return Optional.empty();
        }

        return Optional.of(conversions);
    }

    /**
     * Returns conversion for converting value of source-type to target-type, or throws exception if
     * there's no such conversion.
     */
    public @NotNull TypeConversion getConversionFromDbValue(@NotNull Type source, @NotNull Type target) {
        TypeConversion conversion = findConversionFromDbValue(source, target).orElse(null);
        if (conversion != null)
            return conversion;
        else
            throw new InstantiationFailureException("could not find a conversion from " + source.getTypeName() + " to " + target.getTypeName());
    }

    /**
     * Returns conversion for converting value of source to target, or returns null if there's no such conversion.
     */
    private @NotNull Optional<TypeConversion> findConversionFromDbValue(@NotNull Type source, @NotNull Type target) {
        if (isAssignable(target, source))
            return Optional.of(TypeConversion.identity());

        Optional<TypeConversion> directConversion = typeConversionRegistry.findConversionFromDbValue(source, target);
        if (directConversion.isPresent())
            return directConversion;

        Optional<TypeConversion> arrayConversion = findArrayConversion(source, target);
        if (arrayConversion.isPresent())
            return arrayConversion;

        Optional<TypeConversion> optionalConversion = findOptionalConversion(source, target);
        if (optionalConversion.isPresent())
            return optionalConversion;

        Optional<TypeConversion> enumConversion = findEnumConversion(target);
        if (enumConversion.isPresent())
            return enumConversion;

        return Optional.empty();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static @NotNull Optional<TypeConversion> findEnumConversion(@NotNull Type target) {
        if (isEnum(target)) {
            Class cl = rawType(target).asSubclass(Enum.class);
            return Optional.of(TypeConversion.fromNonNullFunction(value -> Enum.valueOf(cl, value.toString())));
        }

        return Optional.empty();
    }

    private @NotNull Optional<TypeConversion> findArrayConversion(@NotNull Type source, @NotNull Type target) {
        Class<?> rawTarget = rawType(target);

        if (isAssignable(Array.class, source)) {
            if (rawTarget.equals(Set.class))
                return Optional.of(SqlArrayConversion.sqlArray(typeParameter(target), this, LinkedHashSet::new));

            if (rawTarget.isAssignableFrom(List.class))
                return Optional.of(SqlArrayConversion.sqlArray(typeParameter(target), this, Function.identity()));

            if (rawTarget.isArray())
                return Optional.of(SqlArrayConversion.sqlArray(rawTarget.getComponentType(), this, list -> arrayOfType(rawTarget.getComponentType(), list)));
        }

        return Optional.empty();
    }

    private @NotNull Optional<TypeConversion> findOptionalConversion(@NotNull Type source, @NotNull Type target) {
        Class<?> rawTarget = rawType(target);

        if (rawTarget == Optional.class) {
            return optionalConversion(source, typeParameter(target), Optional::ofNullable);

        } else if (rawTarget == OptionalInt.class) {
            return optionalConversion(source, Integer.class, OptionalUtils::optionalIntOfNullable);

        } else if (rawTarget == OptionalLong.class) {
            return optionalConversion(source, Long.class, OptionalUtils::optionalLongOfNullable);

        } else if (rawTarget == OptionalDouble.class) {
            return optionalConversion(source, Double.class, OptionalUtils::optionalDoubleOfNullable);

        } else {
            return Optional.empty();
        }
    }

    private @NotNull <T> Optional<TypeConversion> optionalConversion(@NotNull Type source, @NotNull Type target, @NotNull Function<T, ?> function) {
        return findConversionFromDbValue(source, target).map(cv -> cv.compose((Function<T, Object>) function::apply));
    }

    private static @Nullable Executable findExplicitInstantiatorReference(@NotNull Class<?> cl) {
        List<Constructor<?>> constructors = Stream.of(cl.getDeclaredConstructors())
                .filter(it -> it.isAnnotationPresent(DalesbredInstantiator.class))
                .collect(toList());
        List<Method> methods = Stream.of(cl.getDeclaredMethods())
                .filter(it -> it.isAnnotationPresent(DalesbredInstantiator.class) && Modifier.isStatic(it.getModifiers()))
                .collect(toList());

        int count = constructors.size() + methods.size();
        if (count > 1)
            throw new InstantiationFailureException("only one constructor/method of " + cl.getName() + " can be marked with @DalesbredInstantiator. Found " + count);
        else if (constructors.size() == 1)
            return constructors.get(0);
        else if (methods.size() == 1) {
            Method method = methods.get(0);
            if (method.getReturnType() != cl)
                throw new InstantiationFailureException("Instantiator method " + method.getName() + " does not return " + cl.getName() + " but " + method.getReturnType());

            return method;
        }
        else
            return null;
    }

    @SuppressWarnings("TypeParameterExtendsFinalClass")
    private static @NotNull Stream<? extends Constructor<?>> candidateConstructorsSortedByDescendingParameterCount(@NotNull Class<?> cl) {
        return Stream.of(cl.getConstructors())
                .filter(ctor -> !ctor.isAnnotationPresent(DalesbredIgnore.class))
                .sorted(comparing((Constructor<?> ctor) -> ctor.getParameterTypes().length).reversed());
    }

    public @NotNull TypeConversionRegistry getTypeConversionRegistry() {
        return typeConversionRegistry;
    }

    public @NotNull Dialect getDialect() {
        return dialect;
    }
}
