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

import java.lang.reflect.*;

public final class TypeUtils {

    private TypeUtils() { }

    public static @NotNull Class<?> rawType(@NotNull Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;

        } else if (type instanceof ParameterizedType) {
            return rawType(((ParameterizedType) type).getRawType());

        } else if (type instanceof TypeVariable<?>) {
            // We could return one of the bounds, but it won't work if there are multiple bounds.
            // Therefore just return object.
            return Object.class;

        } else if (type instanceof WildcardType) {
            return rawType(((WildcardType) type).getUpperBounds()[0]);

        } else if (type instanceof GenericArrayType) {
            return arrayType(rawType(((GenericArrayType) type).getGenericComponentType()));

        } else {
            throw new IllegalArgumentException("unexpected type: " + type);
        }
    }

    public static @NotNull Type typeParameter(@NotNull Type type) {
        if (type instanceof ParameterizedType)
            return ((ParameterizedType) type).getActualTypeArguments()[0];

        return Object.class;
    }

    public static boolean isEnum(@NotNull Type type) {
        return (type instanceof Class<?>) && ((Class<?>) type).isEnum();
    }

    public static boolean isPrimitive(@NotNull Type type) {
        return (type instanceof Class<?>) && ((Class<?>) type).isPrimitive();
    }

    public static @NotNull Class<?> arrayType(@NotNull Class<?> type) {
        return Array.newInstance(type, 0).getClass();
    }

    public static @Nullable Type genericSuperClass(@NotNull Type type) {
        return rawType(type).getGenericSuperclass();
    }

    public static @NotNull Type[] genericInterfaces(@NotNull Type type) {
        return rawType(type).getGenericInterfaces();
    }

    public static boolean isAssignable(@NotNull Type target, @NotNull Type source) {
        // TODO: implement proper rules for generic types
        return isAssignableByBoxing(rawType(target), rawType(source));
    }

    private static boolean isAssignableByBoxing(@NotNull Class<?> target, @NotNull Class<?> source) {
        return Primitives.wrap(target).isAssignableFrom(Primitives.wrap(source));
    }
}
