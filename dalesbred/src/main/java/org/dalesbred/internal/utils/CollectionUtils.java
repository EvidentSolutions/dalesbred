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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

import static java.util.stream.Collectors.toCollection;

public final class CollectionUtils {

    private CollectionUtils() { }

    public static @NotNull <A,B> List<B> mapToList(@NotNull Collection<? extends A> xs, @NotNull Function<? super A, ? extends B> mapper) {
        return xs.stream().map(mapper).collect(toListWithCapacity(xs.size()));
    }

    public static @NotNull Object arrayOfType(@NotNull Class<?> elementType, @NotNull List<?> values) {
        int length = values.size();

        Object result = Array.newInstance(elementType, length);
        for (int i = 0; i < length; i++)
            Array.set(result, i, values.get(i));

        return result;
    }

    private static @NotNull <T> Collector<T, ?, ArrayList<T>> toListWithCapacity(int capacity) {
        return toCollection(() -> new ArrayList<>(capacity));
    }
}
