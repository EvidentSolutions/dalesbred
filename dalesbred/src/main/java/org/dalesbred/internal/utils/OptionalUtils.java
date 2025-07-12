/*
 * Copyright (c) 2017 Evident Solutions Oy
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

package org.dalesbred.internal.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class OptionalUtils {

    private OptionalUtils() {
    }

    /**
     * If object is an empty optional-type, return null. If object is non-empty
     * optional-type, return its value. Otherwise return object as it is.
     */
    public static @Nullable Object unwrapOptionalAsNull(@Nullable Object o) {
        return o instanceof Optional<?> ? unwrap((Optional<?>) o)
                : o instanceof OptionalInt ? unwrap((OptionalInt) o)
                : o instanceof OptionalLong ? unwrap((OptionalLong) o)
                : o instanceof OptionalDouble ? unwrap((OptionalDouble) o)
                : o;
    }

    private static @Nullable <T> T unwrap(@NotNull Optional<T> o) {
        return o.orElse(null);
    }

    private static @Nullable Object unwrap(@NotNull OptionalInt o) {
        return o.isPresent() ? o.getAsInt() : null;
    }

    private static @Nullable Object unwrap(@NotNull OptionalLong o) {
        return o.isPresent() ? o.getAsLong() : null;
    }

    private static @Nullable Object unwrap(@NotNull OptionalDouble o) {
        return o.isPresent() ? o.getAsDouble() : null;
    }

    public static @NotNull OptionalInt optionalIntOfNullable(@Nullable Integer v) {
        return v != null ? OptionalInt.of(v) : OptionalInt.empty();
    }

    public static @NotNull OptionalLong optionalLongOfNullable(@Nullable Long v) {
        return v != null ? OptionalLong.of(v) : OptionalLong.empty();
    }

    public static @NotNull OptionalDouble optionalDoubleOfNullable(@Nullable Double v) {
        return v != null ? OptionalDouble.of(v) : OptionalDouble.empty();
    }
}
