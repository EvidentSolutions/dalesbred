/*
 * Copyright (c) 2015 Evident Solutions Oy
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

package org.dalesbred.instantiation;

import org.dalesbred.annotation.DalesbredIgnore;
import org.dalesbred.dialect.Dialect;
import org.dalesbred.integration.java8.JavaTimeTypeConversions;
import org.dalesbred.integration.joda.JodaTypeConversions;
import org.dalesbred.integration.threeten.ThreeTenTypeConversions;
import org.dalesbred.internal.utils.OptionalUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.sql.Array;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isPublic;
import static java.util.Arrays.sort;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static org.dalesbred.internal.utils.CollectionUtils.arrayOfType;
import static org.dalesbred.internal.utils.TypeUtils.*;

/**
 * Provides {@link Instantiator}s for classes.
 */
public final class DefaultInstantiatorRegistry implements InstantiatorRegistry {

    @NotNull
    private final Dialect dialect;

    @NotNull
    private final DefaultTypeConversionRegistry typeConversionRegistry = new DefaultTypeConversionRegistry();

    @NotNull
    private final Map<Type, Instantiator<?>> instantiators = new HashMap<>();

    @NotNull
    private static final Logger log = Logger.getLogger(DefaultInstantiatorRegistry.class.getName());

    public DefaultInstantiatorRegistry(@NotNull Dialect dialect) {
        this.dialect = requireNonNull(dialect);

        DefaultTypeConversions.register(typeConversionRegistry);

        if (JavaTimeTypeConversions.hasJavaTime()) {
            log.fine("Detected java.time in classpath. Registering type conversions for it.");
            JavaTimeTypeConversions.register(typeConversionRegistry);
        }

        if (JodaTypeConversions.hasJoda()) {
            log.fine("Detected Joda Time in classpath. Registering type conversions for Joda.");
            JodaTypeConversions.register(typeConversionRegistry);
        }

        if (ThreeTenTypeConversions.hasThreeTen()) {
            log.fine("Detected ThreeTen in classpath. Registering type conversions for it.");
            ThreeTenTypeConversions.register(typeConversionRegistry);
        }
    }

    @Nullable
    public Object valueToDatabase(@Nullable Object value) {
        if (value == null) return null;

        TypeConversion<?, ?> coercion = typeConversionRegistry.findCoercionToDb(value.getClass()).orElse(null);
        if (coercion != null)
            return coercion.unsafeCast(Object.class).convert(value);
        else
            return dialect.valueToDatabase(value);
    }

    /**
     * Returns constructor matching given argument types. Differs from 
     * {@link Class#getConstructor(Class[])} in that this method allows
     * does not require strict match for types, but finds any constructor
     * that is assignable from given types.
     */
    @NotNull
    public <T> Instantiator<T> findInstantiator(@NotNull Type type, @NotNull NamedTypeList types) {
        @SuppressWarnings("unchecked")
        Instantiator<T> registeredInstantiator = (Instantiator<T>) instantiators.get(type);
        if (registeredInstantiator != null)
            return registeredInstantiator;

        // First check if we have an immediate coercion registered. If so, we'll just use that.
        if (types.size() == 1) {
            @SuppressWarnings("unchecked")
            TypeConversion<Object, ? extends T> conversion =
                    (TypeConversion<Object, ? extends T>) findConversionFromDbValue(types.getType(0), type).orElse(null);
            if (conversion != null)
                return args -> conversion.convert(args.getSingleValue());
        }

        @SuppressWarnings("unchecked")
        Class<T> cl = (Class<T>) rawType(type);
        if (!isPublic(cl.getModifiers()))
            throw new InstantiationException(type + " can't be instantiated reflectively because it is not public");

        return candidateConstructorsSortedByDescendingParameterCount(cl)
                .map(ctor -> instantiatorFrom(ctor, types).orElse(null))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new InstantiationException("could not find a way to instantiate " + type + " with parameters " + types));
    }

    /**
     * Returns an instantiator that uses given constructor and given types to create instances,
     * or null if there are no coercions that can be made to instantiate the type.
     */
    @NotNull
    private <T> Optional<Instantiator<T>> instantiatorFrom(@NotNull Constructor<T> constructor, @NotNull NamedTypeList types) {
        if (!isPublic(constructor.getModifiers()))
            return Optional.empty();

        return findTargetTypes(constructor, types).flatMap(targetTypes ->
                resolveCoercions(types, targetTypes).map(conversions ->
                    new ReflectionInstantiator<>(constructor, conversions, createPropertyAccessorsForValuesNotCoveredByConstructor(constructor, types.getNames()))));
    }

    @NotNull
    private static PropertyAccessor[] createPropertyAccessorsForValuesNotCoveredByConstructor(@NotNull Constructor<?> constructor,
                                                                                              @NotNull List<String> names) {
        int constructorParameterCount = constructor.getParameterTypes().length;
        PropertyAccessor[] accessors = new PropertyAccessor[names.size() - constructorParameterCount];

        for (int i = 0; i < accessors.length; i++)
            accessors[i] = createAccessor(i + constructorParameterCount, constructor.getDeclaringClass(), names);

        return accessors;
    }

    @NotNull
    private static PropertyAccessor createAccessor(int index, @NotNull Class<?> cl, @NotNull List<String> names) {
        return PropertyAccessor.findAccessor(cl, names.get(index)).orElseThrow(() ->
            new InstantiationException("Could not find neither setter nor field for '" + names.get(index) + '\''));
    }

    /**
     * Returns the target types that need to have coercions. The types contain first as many constructor
     * parameter types as we have and then the types of properties of object as given by names of result-set.
     */
    @NotNull
    private static Optional<Type[]> findTargetTypes(@NotNull Constructor<?> ctor, @NotNull NamedTypeList resultSetTypes) {
        Type[] constructorParameterTypes = ctor.getGenericParameterTypes();
        if (constructorParameterTypes.length > resultSetTypes.size()) return Optional.empty();
        if (constructorParameterTypes.length == resultSetTypes.size()) return Optional.of(constructorParameterTypes);

        Type[] result = new Type[resultSetTypes.size()];
        System.arraycopy(constructorParameterTypes, 0, result, 0, constructorParameterTypes.length);

        for (int i = constructorParameterTypes.length; i < result.length; i++) {
            Type type = PropertyAccessor.findPropertyType(ctor.getDeclaringClass(), resultSetTypes.getName(i)).orElse(null);
            if (type != null)
                result[i] = type;
            else
                return Optional.empty();
        }

        return Optional.of(result);
    }

    /**
     * Returns the list of coercions that need to be performed to convert sourceTypes
     * to targetTypes, or null if coercions can't be done.
     */
    @NotNull
    private Optional<TypeConversion<Object,?>[]> resolveCoercions(@NotNull NamedTypeList sourceTypes, @NotNull Type[] targetTypes) {
        if (targetTypes.length != sourceTypes.size())
            return Optional.empty();

        TypeConversion<?,?>[] conversions = new TypeConversion[targetTypes.length];

        for (int i = 0; i < targetTypes.length; i++) {
            TypeConversion<?,?> conversion = findConversionFromDbValue(sourceTypes.getType(i), targetTypes[i]).orElse(null);
            if (conversion != null)
                conversions[i] = conversion;
            else
                return Optional.empty();
        }

        @SuppressWarnings("unchecked")
        TypeConversion<Object, ?>[] result = (TypeConversion<Object, ?>[]) conversions;
        return Optional.of(result);
    }

    /**
     * Returns coercion for converting value of source-type to target-type, or throws exception if
     * there's no such coercion.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public <S,T> TypeConversion<? super S, ? extends T> getCoercionFromDbValue(@NotNull Type source, @NotNull Type target) {
        TypeConversion<?, ?> coercion = findConversionFromDbValue(source, target).orElse(null);
        if (coercion != null)
            return (TypeConversion<S,T>) coercion;
        else
            throw new InstantiationException("could not find a conversion from " + source.getTypeName() + " to " + target.getTypeName());
    }

    /**
     * Returns coercion for converting value of source to target, or returns null if there's no such coercion.
     */
    @NotNull
    private Optional<TypeConversion<?, ?>> findConversionFromDbValue(@NotNull Type source, @NotNull Type target) {
        if (isAssignable(target, source))
            return Optional.of(TypeConversion.identity(target));

        Optional<TypeConversion<?,?>> directConversion = typeConversionRegistry.findCoercionFromDbValue(source, target);
        if (directConversion.isPresent())
            return directConversion;

        Optional<TypeConversion<?, ?>> arrayConversion = findArrayConversion(source, target);
        if (arrayConversion.isPresent())
            return arrayConversion;

        Optional<TypeConversion<?, ?>> optionalConversion = findOptionalConversion(source, target);
        if (optionalConversion.isPresent())
            return optionalConversion;

        Optional<TypeConversion<?, ?>> enumConversion = findEnumConversion(target);
        if (enumConversion.isPresent())
            return enumConversion;

        return Optional.empty();
    }

    @NotNull
    private Optional<TypeConversion<?, ?>> findEnumConversion(@NotNull Type target) {
        if (isEnum(target)) {
            @SuppressWarnings("rawtypes")
            Class<? extends Enum> cl = rawType(target).asSubclass(Enum.class);
            return Optional.ofNullable(dialect.getEnumCoercion(cl));
        }

        return Optional.empty();
    }

    @NotNull
    private Optional<TypeConversion<?, ?>> findArrayConversion(@NotNull Type source, @NotNull Type target) {
        Class<?> rawTarget = rawType(target);

        if (isAssignable(Array.class, source)) {
            if (rawTarget.equals(Set.class))
                return Optional.of(new SqlArrayConversion<>(List.class, typeParameter(target), this, LinkedHashSet::new));

            if (rawTarget.isAssignableFrom(List.class))
                return Optional.of(new SqlArrayConversion<>(List.class, typeParameter(target), this, Function.identity()));

            if (rawTarget.isArray())
                return Optional.of(new SqlArrayConversion<>(rawTarget, rawTarget.getComponentType(), this, list -> arrayOfType(rawTarget.getComponentType(), list)));
        }

        return Optional.empty();
    }

    @NotNull
    private Optional<TypeConversion<?, ?>> findOptionalConversion(@NotNull Type source, @NotNull Type target) {
        Class<?> rawTarget = rawType(target);

        if (rawTarget == Optional.class) {
            return optionalConversion(source, typeParameter(target), target, Optional::ofNullable);

        } else if (rawTarget == OptionalInt.class) {
            return optionalConversion(source, Integer.class, target, OptionalUtils::optionalIntOfNullable);

        } else if (rawTarget == OptionalLong.class) {
            return optionalConversion(source, Long.class, target, OptionalUtils::optionalLongOfNullable);

        } else if (rawTarget == OptionalDouble.class) {
            return optionalConversion(source, Double.class, target, OptionalUtils::optionalDoubleOfNullable);

        } else {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private <T> Optional<TypeConversion<?, ?>> optionalConversion(@NotNull Type source, @NotNull Type target, @NotNull Type result, @NotNull Function<T,?> function) {
        return findConversionFromDbValue(source, target)
                .map(cv -> cv.compose(result, v -> function.apply((T) v)));
    }

    @NotNull
    private static <T> Stream<Constructor<T>> candidateConstructorsSortedByDescendingParameterCount(@NotNull Class<T> cl) {
        @SuppressWarnings("unchecked")
        Constructor<T>[] constructors = (Constructor<T>[]) cl.getConstructors();
        sort(constructors, comparing((Constructor<T> ctor) -> ctor.getParameterTypes().length).reversed());

        return Stream.of(constructors)
                .filter(ctor -> !ctor.isAnnotationPresent(DalesbredIgnore.class));
    }

    @NotNull
    public TypeConversionRegistry getTypeConversionRegistry() {
        return typeConversionRegistry;
    }

    @Override
    public <T> void registerInstantiator(@NotNull Class<T> cl, @NotNull Instantiator<T> instantiator) {
        instantiators.put(requireNonNull(cl), requireNonNull(instantiator));
    }
}
