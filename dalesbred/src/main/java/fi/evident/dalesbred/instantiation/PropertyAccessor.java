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

package fi.evident.dalesbred.instantiation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

import static fi.evident.dalesbred.utils.Throwables.propagate;
import static java.lang.reflect.Modifier.isPublic;

abstract class PropertyAccessor {

    private static final Pattern UNDERSCORE_PATTERN = Pattern.compile("_");

    abstract void set(Object object, Object value);

    abstract Class<?> getType();

    @Nullable
    static PropertyAccessor findAccessor(@NotNull Class<?> cl, @NotNull String name) {
        String nameWithoutUnderscore = UNDERSCORE_PATTERN.matcher(name).replaceAll("");
        String[] names = nameWithoutUnderscore.equals(name) ? new String[] { name } : new String[] { name, nameWithoutUnderscore };
        Method setter = findSetter(cl, names);
        if (setter != null) {
            return new SetterPropertyAccessor(setter);
        } else {
            Field field = findField(cl, names);
            if (field != null)
                return new FieldPropertyAccessor(field);
            else
                return null;
        }
    }

    @Nullable
    private static Field findField(@NotNull Class<?> cl, @NotNull String[] names) {
        Field result = null;

        for (Field field : cl.getFields())
            for (String name : names)
                if (isPublic(field.getModifiers()) && field.getName().equalsIgnoreCase(name)) {
                    if (result != null)
                        throw new InstantiationException("Conflicting fields for property: " + result + " - " + names[0]);
                    result = field;
                }

        return result;
    }

    @Nullable
    private static Method findSetter(@NotNull Class<?> cl, @NotNull String[] names) {
        Method result = null;

        for (Method method : cl.getMethods())
            for (String name : names) {
                String methodName = "set" + name;
                if (methodName.equalsIgnoreCase(method.getName()) && isPublic(method.getModifiers()) && method.getParameterTypes().length == 1) {
                    if (result != null)
                        throw new InstantiationException("Conflicting setters for property: " + result + " - " + names[0]);
                    result = method;
                }
            }

        return result;
    }

    @Nullable
    public static Class<?> findPropertyType(@NotNull Class<?> cl, @NotNull String name) {
        PropertyAccessor accessor = findAccessor(cl, name);
        if (accessor != null)
            return accessor.getType();
        else
            return null;
    }

    private static final class FieldPropertyAccessor extends PropertyAccessor {

        @NotNull
        private final Field field;

        private FieldPropertyAccessor(@NotNull Field field) {
            this.field = field;
        }

        @Override
        Class<?> getType() {
            return field.getType();
        }

        @Override
        void set(Object object, Object value) {
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw propagate(e);
            }
        }
    }

    private static final class SetterPropertyAccessor extends PropertyAccessor {

        @NotNull
        private final Method setter;

        private SetterPropertyAccessor(@NotNull Method setter) {
            this.setter = setter;
        }

        @Override
        Class<?> getType() {
            return setter.getParameterTypes()[0];
        }

        @Override
        void set(Object object, Object value) {
            try {
                setter.invoke(object, value);
            } catch (Exception e) {
                throw propagate(e);
            }
        }
    }
}

