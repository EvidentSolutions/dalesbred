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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static fi.evident.dalesbred.utils.Primitives.wrap;
import static fi.evident.dalesbred.utils.Require.requireNonNull;
import static java.lang.reflect.Modifier.isPublic;

/**
 * Provides {@link Instantiator}s for classes.
 */
public final class InstantiatorRegistry {

    private final Dialect dialect;
    private final DefaultTypeConversionRegistry typeConversionRegistry = new DefaultTypeConversionRegistry();
    private static final Logger log = Logger.getLogger(InstantiatorRegistry.class.getName());

    public InstantiatorRegistry(@NotNull Dialect dialect) {
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

        @SuppressWarnings("unchecked")
        TypeConversion<Object, Object> coercion = (TypeConversion) typeConversionRegistry.findCoercionToDb(value.getClass());
        if (coercion != null)
            return coercion.convert(value);
        else
            return dialect.valueToDatabase(value);
    }

    /**
     * Returns constructor matching given argument types. Differs from 
     * {@link Class#getConstructor(Class[])} in that this method allows
     * does not require strict match for types, but finds any constructor
     * that is assignable from given types.
     */
    public <T> Instantiator<T> findInstantiator(Class<T> cl, NamedTypeList types) {
        // First check if we have an immediate coercion registered. If so, we'll just use that.
        if (types.size() == 1) {
            @SuppressWarnings("unchecked")
            TypeConversion<Object, ? extends T> coercion = (TypeConversion) findCoercionFromDbValue(types.getType(0), cl);
            if (coercion != null)
                return new CoercionInstantiator<T>(coercion);
        }

        // If there was no coercion, we try to find a matching constructor, applying coercions to arguments.
        for (Constructor<T> constructor : constructorsFor(cl)) {
            Instantiator<T> instantiator = instantiatorFrom(constructor, types);
            if (instantiator != null)
                return instantiator;
        }

        throw new InstantiationException(cl + " does not have instantiator matching types " + types);
    }

    /**
     * Returns an instantiator that uses given constructor and given types to create instances,
     * or null if there are no coercions that can be made to instantiate the type.
     */
    @Nullable
    private <T> Instantiator<T> instantiatorFrom(Constructor<T> constructor, NamedTypeList types) {
        if (!isPublic(constructor.getModifiers())) return null;

        List<TypeConversion<Object,?>> coercions = resolveCoercions(types, constructor.getParameterTypes());
        if (coercions != null)
            return new ConstructorInstantiator<T>(constructor, coercions);
        else
            return null;
    }

    /**
     * Returns the list of coercions that need to be performed to convert sourceTypes
     * to targetTypes, or null if coercions can't be done.
     */
    @Nullable
    private List<TypeConversion<Object,?>> resolveCoercions(@NotNull NamedTypeList sourceTypes,
                                                      @NotNull Class<?>[] targetTypes) {
        if (targetTypes.length != sourceTypes.size())
            return null;

        List<TypeConversion<Object,?>> coercions = new ArrayList<TypeConversion<Object, ?>>(targetTypes.length);

        for (int i = 0; i < targetTypes.length; i++) {
            @SuppressWarnings("unchecked")
            TypeConversion<Object,?> coercion = (TypeConversion) findCoercionFromDbValue(sourceTypes.getType(i), targetTypes[i]);
            if (coercion != null)
                coercions.add(coercion);
            else
                return null;
        }

        return coercions;
    }

    /**
     * Returns coercion for converting value of source-type to target-type, or throws exception if
     * there's no such coercion.
     */
    @NotNull
    public <S,T> TypeConversion<? super S, ? extends T> getCoercionFromDbValue(@NotNull Class<S> source, @NotNull Class<T> target) {
        TypeConversion<? super S, ? extends T> coercion = findCoercionFromDbValue(source, target);
        if (coercion != null)
            return coercion;
        else
            throw new InstantiationException("could not find a conversion from " + source.getName() + " to " + target.getName());
    }

    /**
     * Returns coercion for converting value of source to target, or returns null if there's no such coercion.
     */
    @Nullable
    private <S,T> TypeConversion<? super S, ? extends T> findCoercionFromDbValue(@NotNull Class<S> source, @NotNull Class<T> target) {
        if (wrap(target).isAssignableFrom(wrap(source)))
            return TypeConversion.identity().cast(source, target);

        TypeConversion<?,?> coercion = typeConversionRegistry.findCoercionFromDbValue(source, target);
        if (coercion != null)
            return coercion.cast(source, target);

        if (target.isEnum())
            return dialect.getEnumCoercion(target.asSubclass(Enum.class)).cast(source, target);

        return null;
    }

    @NotNull
    private static <T> Constructor<T>[] constructorsFor(@NotNull Class<T> cl) {
        @SuppressWarnings("unchecked")
        Constructor<T>[] constructors = (Constructor<T>[]) cl.getConstructors();
        return constructors;
    }

    @NotNull
    public TypeConversionRegistry getTypeConversionRegistry() {
        return typeConversionRegistry;
    }
}
