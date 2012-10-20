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

import static fi.evident.dalesbred.utils.Primitives.isAssignableByBoxing;
import static fi.evident.dalesbred.utils.Primitives.wrap;
import static fi.evident.dalesbred.utils.Require.requireNonNull;

/**
 * A conversion from S into T.
 */
public abstract class TypeConversion<S,T> {

    @NotNull
    private final Class<S> source;

    @NotNull
    private final Class<T> target;

    @SuppressWarnings("unchecked")
    public TypeConversion(@NotNull Class<S> source, @NotNull Class<T> target) {
        this.source = (Class) wrap(requireNonNull(source));
        this.target = (Class) wrap(requireNonNull(target));
    }

    @NotNull
    public Class<S> getSource() {
        return source;
    }

    @NotNull
    public Class<T> getTarget() {
        return target;
    }

    /**
     * Converts value of type {@code S} to value of type {@code T}.
     */
    @NotNull
    public abstract T convert(@NotNull S value);

    /**
     * Returns identity-coercion, ie. a coercion that does nothing.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> TypeConversion<T,T> identity(@NotNull Class<T> type) {
        return new TypeConversion<T, T>(type, type) {
            @NotNull
            @Override
            public T convert(@NotNull T value) {
                return value;
            }

            @NotNull
            @Override
            public String toString() {
                return "IdentityConversion";
            }
        };
    }

    /**
     * Safely casts this coercion into a coercion from {@code S} to {@code T} or throws an exception
     * if coercion is not compatible.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public <S,T> TypeConversion<S,T> cast(@NotNull Class<S> source, @NotNull Class<T> target) {
        if (canConvert(source, target))
            return (TypeConversion) this;
        else
            throw new RuntimeException("can't cast " + this + " to coercion from " + source.getName() + " to " + target.getName());
    }

    @NotNull
    @Override
    public String toString() {
        return getClass().getName() + " [" + source.getName() + " -> " + target.getName() + "]";
    }

    /**
     * Returns true if this coercion knows hows to convert instances of {@code source} to
     * instances of {@code target}.
     */
    private boolean canConvert(@NotNull Class<?> source, @NotNull Class<?> target) {
        return isAssignableByBoxing(this.source, source) && isAssignableByBoxing(target, this.target);
    }

}
