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

import org.jetbrains.annotations.NotNull;

/**
 * A conversion from S into T.
 *
 * @see TypeConversionBase
 */
public abstract class TypeConversion<S,T> {

    private static final IdentityTypeConversion IDENTITY = new IdentityTypeConversion();

    /**
     * Returns true if this coercion knows hows to convert instances of {@code source} to
     * instances of {@code target}.
     */
    @NotNull
    public abstract boolean canConvert(@NotNull Class<?> source, @NotNull Class<?> target);

    /**
     * Converts value of type {@code S} to value of type {@code T}.
     */
    @NotNull
    public abstract T convert(@NotNull S value);

    /**
     * Returns identity-coercion, ie. a coercion that does nothing.
     */
    @SuppressWarnings("unchecked")
    public static <S> TypeConversion<S,S> identity() {
        return IDENTITY;
    }

    /**
     * Safely casts this coercion into a coercion from {@code S} to {@code T} or throws an exception
     * if coercion is not compatible.
     */
    @SuppressWarnings("unchecked")
    public <S,T> TypeConversion<S,T> cast(Class<S> source, Class<T> target) {
        if (canConvert(source, target))
            return (TypeConversion) this;
        else
            throw new RuntimeException("can't cast " + this + " to coercion from " + source.getName() + " to " + target.getName());
    }

    private static final class IdentityTypeConversion<T> extends TypeConversion<T, T> {

        @NotNull
        @Override
        public boolean canConvert(@NotNull Class<?> source, @NotNull Class<?> target) {
            return true;
        }

        @NotNull
        @Override
        public T convert(@NotNull T value) {
            return value;
        }
    }
}
