/*
 * Copyright (c) 2013 Evident Solutions Oy
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

package fi.evident.dalesbred;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import static java.lang.reflect.Modifier.isPublic;

final class NamedParameterValueProviders {
    @NotNull
    static NamedParameterValueProvider providerForMap(@NotNull final Map<String, ?> valueMap) {
        return new NamedParameterValueProvider() {
            @Nullable
            @Override
            public Object getValue(@NotNull String parameterName) throws IllegalArgumentException {
                Object value = valueMap.get(parameterName);
                if (value != null || valueMap.containsKey(parameterName))
                    return value;
                else
                    throw new IllegalArgumentException("No value registered for key '" + parameterName + '\'');
            }
        };
    }

    @NotNull
    static NamedParameterValueProvider providerForBean(@NotNull final Object object) {
        return new NamedParameterValueProvider() {
            @Nullable
            @Override
            public Object getValue(@NotNull String parameterName) throws IllegalArgumentException {
                try {
                    Method getter = findGetter(object.getClass(), parameterName);
                    if (getter != null) {
                        return getter.invoke(object);
                    } else {
                        Field field = findField(object.getClass(), parameterName);
                        if (field != null)
                            return field.get(object);
                        else
                            throw new IllegalArgumentException("Accessor not found for: " + parameterName);
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
        };
    }

    @Nullable
    private static Field findField(@NotNull Class<?> cl, @NotNull String name) {
        for (Field field : cl.getFields())
            if (isPublic(field.getModifiers()) && field.getName().equalsIgnoreCase(name))
                return field;

        return null;
    }

    @Nullable
    private static Method findGetter(@NotNull Class<?> cl, @NotNull String name) {
        String[] methodNames =  { "is" + name, "get" + name };

        for (Method method : cl.getMethods())
            for (String methodName : methodNames)
                if (methodName.equalsIgnoreCase(method.getName()) && isPublic(method.getModifiers()) && method.getParameterTypes().length == 0)
                    return method;

        return null;
    }

    private NamedParameterValueProviders() {}
}
