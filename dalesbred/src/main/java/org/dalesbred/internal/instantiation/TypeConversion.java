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

package org.dalesbred.internal.instantiation;

import org.dalesbred.internal.utils.Primitives;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * A conversion from S into T.
 */
public abstract class TypeConversion {

    @NotNull
    private final Type source;

    @NotNull
    private final Type target;

    public TypeConversion(@NotNull Type source, @NotNull Type target) {
        this.source = Primitives.wrap(requireNonNull(source));
        this.target = Primitives.wrap(requireNonNull(target));
    }

    @NotNull
    public static <S,T> TypeConversion fromNonNullFunction(@NotNull Class<S> source, @NotNull Class<T> target, @NotNull Function<S, T> function) {
        return new TypeConversion(source, target) {

            @Nullable
            @Override
            public Object convert(@Nullable Object value) {
                return value != null ? function.apply(source.cast(value)) : null;
            }
        };
    }

    @NotNull
    public Type getSource() {
        return source;
    }

    @NotNull
    public Type getTarget() {
        return target;
    }

    @Nullable
    public abstract Object convert(@Nullable Object value);

    @NotNull
    public <T,R> TypeConversion compose(@NotNull Type result, @NotNull Function<T,R> function) {
        TypeConversion self = this;

        return new TypeConversion(source, result) {
            @SuppressWarnings("unchecked")
            @Nullable
            @Override
            public Object convert(@Nullable Object value) {
                return function.apply((T) self.convert(value));
            }
        };
    }

    /**
     * Returns identity-coercion, ie. a coercion that does nothing.
     */
    @NotNull
    public static TypeConversion identity(@NotNull Type type) {
        return new TypeConversion(type, type) {
            @Override
            @Nullable
            public Object convert(@Nullable Object value) {
                return value;
            }
        };
    }

    @NotNull
    @Override
    public String toString() {
        return getClass().getName() + " [" + source + " -> " + target + ']';
    }
}
