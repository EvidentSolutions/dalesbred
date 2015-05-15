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

package org.dalesbred.internal.utils;

import org.dalesbred.DatabaseException;
import org.dalesbred.internal.instantiation.InstantiationException;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

public final class EnumUtils {

    private EnumUtils() { }

    @NotNull
    public static <T extends Enum<T>> T enumByOrdinal(@NotNull Class<T> enumType, int ordinal) {
        Enum<?>[] constants = enumType.getEnumConstants();
        if (ordinal >= 0 && ordinal < constants.length)
            return enumType.cast(constants[ordinal]);
        else
            throw new DatabaseException("invalid ordinal " + ordinal + " for enum type " + enumType.getName());
    }

    @NotNull
    public static <T extends Enum<T>,K> T enumByKey(@NotNull Class<T> enumType, @NotNull Function<T, K> keyFunction, @NotNull K key) {
        for (T enumConstant : enumType.getEnumConstants())
            if (Objects.equals(key, keyFunction.apply(enumConstant)))
                return enumConstant;

        throw new InstantiationException("could not find enum constant of type " + enumType.getName() + " for " + key);
    }
}
