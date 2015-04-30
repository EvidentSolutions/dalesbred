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

import org.dalesbred.internal.utils.Primitives;
import org.dalesbred.internal.utils.TypeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * A conversion from S into T.
 */
public abstract class TypeConversion<S,T> {

    @NotNull
    private final Type source;

    @NotNull
    private final Type target;

    public TypeConversion(@NotNull Type source, @NotNull Type target) {
        this.source = Primitives.wrap(requireNonNull(source));
        this.target = Primitives.wrap(requireNonNull(target));
    }

    @NotNull
    public static <S,T> TypeConversion<S,T> fromNonNullFunction(@NotNull Class<S> source, @NotNull Class<T> target, @NotNull Function<S, T> function) {
        return fromFunction(source, target, value -> value != null ? function.apply(value) : null);
    }

    @NotNull
    public static <S,T> TypeConversion<S,T> fromFunction(@NotNull Class<S> source, @NotNull Class<T> target, @NotNull Function<S, T> function) {
        return new TypeConversion<S, T>(source, target) {

            @Nullable
            @Override
            public T convert(@Nullable S value) {
                return function.apply(value);
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
    public abstract T convert(@Nullable S value);

    @NotNull
    public <R> TypeConversion<S,R> compose(@NotNull Type result, @NotNull Function<T,R> function) {
        TypeConversion<S,T> self = this;

        return new TypeConversion<S, R>(source, result) {
            @Nullable
            @Override
            public R convert(@Nullable S value) {
                return function.apply(self.convert(value));
            }
        };
    }

    /**
     * Returns identity-coercion, ie. a coercion that does nothing.
     */
    @NotNull
    public static <T> TypeConversion<T,T> identity(@NotNull Type type) {
        return new TypeConversion<T, T>(type, type) {
            @Override
            @Nullable
            public T convert(@Nullable T value) {
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
    private <S,T> TypeConversion<S,T> cast(@NotNull Type requiredSource, @NotNull Type requiredTarget) {
        if (canConvert(requiredSource, requiredTarget)) {
            @SuppressWarnings("unchecked")
            TypeConversion<S,T> result = (TypeConversion<S,T>) this;
            return result;
        } else
            throw new RuntimeException("can't cast " + this + " to coercion from " + requiredSource + " to " + requiredTarget);
    }

    @NotNull
    public <R> TypeConversion<Object,R> unsafeCast(@NotNull Class<R> requiredTarget) {
        TypeConversion<S,R> self = cast(source, requiredTarget);
        return new TypeConversion<Object, R>(Object.class, requiredTarget) {
            @SuppressWarnings("unchecked")
            @Nullable
            @Override
            public R convert(@Nullable Object value) {
                return self.convert((S) value);
            }
        };
    }

    @NotNull
    @Override
    public String toString() {
        return getClass().getName() + " [" + source + " -> " + target + ']';
    }

    /**
     * Returns true if this coercion knows hows to convert instances of {@code requiredSource} to
     * instances of {@code requiredTarget}.
     */
    private boolean canConvert(@NotNull Type requiredSource, @NotNull Type requiredTarget) {
        return TypeUtils.isAssignable(source, requiredSource) && TypeUtils.isAssignable(requiredTarget, target);
    }
}
