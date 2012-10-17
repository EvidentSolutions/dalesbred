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

package fi.evident.dalesbred.utils;

import org.jetbrains.annotations.NotNull;

/**
 * Utilities for handling primitive types and their wrappers uniformly.
 */
public final class Primitives {

    private Primitives() { }

    /**
     * Returns the corresponding wrapper type for a primitive type, or the type itself if it is not a primitive type.
     */
    @NotNull
    public static Class<?> wrap(@NotNull Class<?> type) {
        return (type == boolean.class) ? Boolean.class
             : (type == byte.class)    ? Byte.class
             : (type == char.class)    ? Character.class
             : (type == short.class)   ? Short.class
             : (type == int.class)     ? Integer.class
             : (type == long.class)    ? Long.class
             : (type == float.class)   ? Float.class
             : (type == double.class)  ? Double.class
             : type;
    }

    /**
     * Returns the corresponding primitive type for a wrapper type, or the type itself if it is not a wrapper.
     */
    @NotNull
    public static Class<?> unwrap(@NotNull Class<?> type) {
        return (type == Boolean.class)   ? boolean.class
             : (type == Byte.class)      ? byte.class
             : (type == Character.class) ? char.class
             : (type == Short.class)     ? short.class
             : (type == Integer.class)   ? int.class
             : (type == Long.class)      ? long.class
             : (type == Float.class)     ? float.class
             : (type == Double.class)    ? double.class
             : type;
    }

    public static boolean isAssignableByBoxing(@NotNull Class<?> target, @NotNull Class<?> source) {
        return wrap(target).isAssignableFrom(wrap(source));
    }
}
