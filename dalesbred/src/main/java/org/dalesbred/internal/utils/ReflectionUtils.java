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

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import static java.lang.reflect.Modifier.isPublic;

public final class ReflectionUtils {

    private ReflectionUtils() {
    }

    @NotNull
    public static Optional<Field> findField(@NotNull Class<?> cl, @NotNull String name) {
        try {
            return Optional.of(cl.getField(name));
        } catch (NoSuchFieldException e) {
            return Optional.empty();
        }
    }

    @NotNull
    public static Optional<Method> findGetter(@NotNull Class<?> cl, @NotNull String propertyName) {
        String capitalizedName = StringUtils.capitalize(propertyName);

        try {
            return Optional.of(cl.getMethod("get" + capitalizedName));
        } catch (NoSuchMethodException e) {
            try {
                return Optional.of(cl.getMethod("is" + capitalizedName));
            } catch (NoSuchMethodException e1) {
                return Optional.empty();
            }
        }
    }

    public static void makeAccessible(@NotNull Constructor<?> constructor) throws SecurityException {
        if(!isPublic(constructor.getModifiers()))
            constructor.setAccessible(true);
    }
}
