/*
 * Copyright (c) 2012 Evident Solutions Oy
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

package fi.evident.dalesbred.instantiation;

import fi.evident.dalesbred.dialects.Dialect;
import fi.evident.dalesbred.support.joda.JodaTypeConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import static fi.evident.dalesbred.utils.Primitives.isAssignableByBoxing;
import static fi.evident.dalesbred.utils.Require.requireNonNull;
import static java.lang.reflect.Modifier.isPublic;
import static java.util.Arrays.sort;

/**
 * Provides {@link Instantiator}s for classes.
 */
public final class DefaultInstantiatorRegistry implements InstantiatorRegistry {

    @NotNull
    private final Dialect dialect;

    @NotNull
    private final DefaultTypeConversionRegistry typeConversionRegistry = new DefaultTypeConversionRegistry();

    @Nullable
    private InstantiationListeners instantiationListeners;

    @NotNull
    private static final Logger log = Logger.getLogger(DefaultInstantiatorRegistry.class.getName());

    public DefaultInstantiatorRegistry(@NotNull Dialect dialect) {
        this.dialect = requireNonNull(dialect);

        DefaultTypeConversions.register(typeConversionRegistry);

        if (JodaTypeConversions.hasJoda()) {
            log.fine("Detected Joda Time in classpath. Registering type conversions for Joda.");
            JodaTypeConversions.register(typeConversionRegistry);
        }
    }

    @Nullable
    public Object valueToDatabase(@Nullable Object value) {
        if (value == null) return null;

        TypeConversion<?, ?> coercion = typeConversionRegistry.findCoercionToDb(value.getClass());
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
    public <T> Instantiator<T> findInstantiator(@NotNull Class<T> cl, @NotNull NamedTypeList types) {
        // First check if we have an immediate coercion registered. If so, we'll just use that.
        if (types.size() == 1) {
            TypeConversion<Object, ? extends T> coercion = findConversionFromDbValue(types.getType(0), cl);
            if (coercion != null)
                return new CoercionInstantiator<T>(coercion, instantiationListeners);
        }

        if (!isPublic(cl.getModifiers()))
            throw new InstantiationException(cl + " can't be instantiated reflectively because it is not public");

        // If there was no coercion, we try to find a matching constructor, applying coercions to arguments.
        for (Constructor<T> constructor : constructorsSortedByDescendingParameterCount(cl)) {
            Instantiator<T> instantiator = instantiatorFrom(constructor, types);
            if (instantiator != null)
                return instantiator;
        }

        throw new InstantiationException("could not find a way to instantiate " + cl + " with parameters " + types);
    }

    /**
     * Returns an instantiator that uses given constructor and given types to create instances,
     * or null if there are no coercions that can be made to instantiate the type.
     */
    @Nullable
    private <T> Instantiator<T> instantiatorFrom(@NotNull Constructor<T> constructor, @NotNull NamedTypeList types) {
        if (!isPublic(constructor.getModifiers())) return null;

        Class<?>[] targetTypes = findTargetTypes(constructor, types);
        if (targetTypes == null)
            return null;

        TypeConversion<Object, ?>[] conversions = resolveCoercions(types, targetTypes);
        if (conversions != null) {
            PropertyAccessor[] accessors = createPropertyAccessorsForValuesNotCoveredByConstructor(constructor, types.getNames());
            return new ReflectionInstantiator<T>(constructor, conversions, accessors, instantiationListeners);
        } else
            return null;
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
        PropertyAccessor accessor = PropertyAccessor.findAccessor(cl, names.get(index));
        if (accessor != null)
            return accessor;
        else
            throw new InstantiationException("Could not find neither setter nor field for '" + names.get(index) + '\'');
    }

    /**
     * Returns the target types that need to have coercions. The types contain first as many constructor
     * parameter types as we have and then the types of properties of object as given by names of result-set.
     */
    @Nullable
    private static Class<?>[] findTargetTypes(@NotNull Constructor<?> ctor, @NotNull NamedTypeList resultSetTypes) {
        Class<?>[] constructorParameterTypes = ctor.getParameterTypes();
        if (constructorParameterTypes.length > resultSetTypes.size()) return null;
        if (constructorParameterTypes.length == resultSetTypes.size()) return constructorParameterTypes;

        Class<?>[] result = new Class<?>[resultSetTypes.size()];
        System.arraycopy(constructorParameterTypes, 0, result, 0, constructorParameterTypes.length);

        for (int i = constructorParameterTypes.length; i < result.length; i++) {
            Class<?> type = PropertyAccessor.findPropertyType(ctor.getDeclaringClass(), resultSetTypes.getName(i));
            if (type != null)
                result[i] = type;
           else
                 return null;
        }

        return result;
    }

    /**
     * Returns the list of coercions that need to be performed to convert sourceTypes
     * to targetTypes, or null if coercions can't be done.
     */
    @Nullable
    private TypeConversion<Object,?>[] resolveCoercions(@NotNull NamedTypeList sourceTypes, @NotNull Class<?>[] targetTypes) {
        if (targetTypes.length != sourceTypes.size())
            return null;

        @SuppressWarnings("unchecked")
        TypeConversion<Object,?>[] conversions = new TypeConversion[targetTypes.length];

        for (int i = 0; i < targetTypes.length; i++) {
            TypeConversion<Object,?> conversion = findConversionFromDbValue(sourceTypes.getType(i), targetTypes[i]);
            if (conversion != null)
                conversions[i] = conversion;
            else
                return null;
        }

        return conversions;
    }

    /**
     * Returns coercion for converting value of source-type to target-type, or throws exception if
     * there's no such coercion.
     */
    @NotNull
    public <S,T> TypeConversion<? super S, ? extends T> getCoercionFromDbValue(@NotNull Class<S> source, @NotNull Class<T> target) {
        TypeConversion<? super S, ? extends T> coercion = findConversionFromDbValue(source, target);
        if (coercion != null)
            return coercion;
        else
            throw new InstantiationException("could not find a conversion from " + source.getName() + " to " + target.getName());
    }

    /**
     * Returns coercion for converting value of source to target, or returns null if there's no such coercion.
     */
    @Nullable
    private <T> TypeConversion<Object, ? extends T> findConversionFromDbValue(@NotNull Class<?> source, @NotNull Class<T> target) {
        if (isAssignableByBoxing(target, source))
            return TypeConversion.identity(target).unsafeCast(target);

        TypeConversion<?,?> coercion = typeConversionRegistry.findCoercionFromDbValue(source, target);
        if (coercion != null)
            return coercion.unsafeCast(target);

        if (target.isEnum())
            return dialect.getEnumCoercion(target.asSubclass(Enum.class)).unsafeCast(target);

        return null;
    }

    @NotNull
    private static <T> Constructor<T>[] constructorsSortedByDescendingParameterCount(@NotNull Class<T> cl) {
        @SuppressWarnings("unchecked")
        Constructor<T>[] constructors = (Constructor<T>[]) cl.getConstructors();

        sort(constructors, new Comparator<Constructor<T>>() {
            @Override
            public int compare(Constructor<T> o1, Constructor<T> o2) {
                int c1 = o1.getParameterTypes().length;
                int c2 = o2.getParameterTypes().length;
                return (c1 < c2) ? 1
                     : (c1 > c2) ? -1
                     : 0;
            }
        });
        return constructors;
    }

    @NotNull
    public TypeConversionRegistry getTypeConversionRegistry() {
        return typeConversionRegistry;
    }

    /**
     * Adds a new listener which gets notified whenever an object is instantiated.
     */
    @Override
    public void addInstantiationListener(@NotNull InstantiationListener instantiationListener) {
        if (instantiationListeners == null) {
            instantiationListeners = new InstantiationListeners();
            instantiationListeners.add(instantiationListener);
        }
    }

    private static final class InstantiationListeners implements InstantiationListener {

        private final List<InstantiationListener> listeners = new ArrayList<InstantiationListener>();

        public void add(@NotNull InstantiationListener listener) {
            listeners.add(requireNonNull(listener));
        }

        @Override
        public void onInstantiation(@NotNull Object object) {
            for (InstantiationListener listener : listeners)
                listener.onInstantiation(object);
        }
    }
}
