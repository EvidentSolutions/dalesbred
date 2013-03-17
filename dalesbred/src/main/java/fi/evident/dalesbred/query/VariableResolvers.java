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

package fi.evident.dalesbred.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static fi.evident.dalesbred.utils.ReflectionUtils.findField;
import static fi.evident.dalesbred.utils.ReflectionUtils.findGetter;

/**
 * Some useful {@link VariableResolver} implementations.
 */
public final class VariableResolvers {

    private VariableResolvers() {
    }

    /**
     * Returns a {@link VariableResolver} that is backed by given map.
     */
    @NotNull
    public static VariableResolver resolverForMap(@NotNull final Map<String, ?> variables) {
        return new VariableResolver() {
            @Nullable
            @Override
            public Object getValue(@NotNull String variable) {
                Object value = variables.get(variable);
                if (value != null || variables.containsKey(variable))
                    return value;
                else
                    throw new VariableResolutionException("No value registered for key '" + variable + '\'');
            }
        };
    }

    /**
     * Returns a {@link VariableResolver} that is backed by given bean. When variables are looked up,
     * tries to find a matching getter or accessible field for the variable and returns its value.
     */
    @NotNull
    public static VariableResolver resolverForBean(@NotNull final Object object) {
        return new VariableResolver() {
            @Nullable
            @Override
            public Object getValue(@NotNull String variable) {
                try {
                    Method getter = findGetter(object.getClass(), variable);
                    if (getter != null) {
                        return getter.invoke(object);
                    } else {
                        Field field = findField(object.getClass(), variable);
                        if (field != null)
                            return field.get(object);
                        else
                            throw new VariableResolutionException("No accessor found for '" + variable + '\'');
                    }
                } catch (InvocationTargetException e) {
                    throw new VariableResolutionException("Failed to resolve variable '" + variable + "': " + e.getTargetException(), e.getTargetException());
                } catch (IllegalAccessException e) {
                    throw new VariableResolutionException("Could not access variable'" + variable + '\'', e);
                }
            }
        };
    }
}
