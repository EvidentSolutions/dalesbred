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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * A conversion from S into T.
 */
public class TypeConversion {

    @NotNull
    private final Function<Object,Object> conversion;

    @SuppressWarnings("unchecked")
    private TypeConversion(@NotNull Function<?, ?> conversion) {
        this.conversion = (Function<Object,Object>) conversion;
    }

    @NotNull
    public static <S,T> TypeConversion fromNonNullFunction(@NotNull Function<S, T> function) {
        return new TypeConversion((S value) -> value != null ? function.apply(value) : null);
    }

    /**
     * Returns identity-coercion, ie. a coercion that does nothing.
     */
    @NotNull
    public static TypeConversion identity() {
        return fromNonNullFunction(Function.identity());
    }

    @Nullable
    public Object convert(@Nullable Object value) {
        return conversion.apply(value);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public TypeConversion compose(@NotNull Function<?,?> function) {
        return new TypeConversion(conversion.andThen((Function<Object,Object>) function));
    }
}
