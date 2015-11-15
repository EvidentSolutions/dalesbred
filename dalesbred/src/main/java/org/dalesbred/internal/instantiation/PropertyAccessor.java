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

import org.dalesbred.annotation.DalesbredIgnore;
import org.dalesbred.internal.utils.Throwables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.lang.reflect.Modifier.isPublic;
import static org.dalesbred.internal.utils.StringUtils.isEqualIgnoringCaseAndUnderscores;

abstract class PropertyAccessor {

    abstract void set(Object object, Object value);

    abstract Type getType();

    @NotNull
    private static final Pattern PERIOD = Pattern.compile("\\.");

    @NotNull
    static Optional<PropertyAccessor> findAccessor(@NotNull Class<?> cl, @NotNull String path) {
        String[] segments = PERIOD.split(path, -1);

        assert segments.length != 0; // split will always return non-empty array

        if (segments.length == 1)
            return findFinalAccessor(cl, segments[0]);

        Class<?> currentClass = cl;

        PropertyReader[] readers = new PropertyReader[segments.length - 1];
        for (int i = 0; i < segments.length - 1; i++) {
            Field field = findField(currentClass, segments[i]).orElse(null);
            if (field != null) {
                readers[i] = field::get;
                currentClass = field.getType();
            } else {
                Method getter = findGetter(currentClass, segments[i]).orElse(null);
                if (getter != null) {
                    readers[i] = getter::invoke;
                    currentClass = getter.getReturnType();
                } else {
                    return Optional.empty();
                }
            }
        }

        Optional<PropertyAccessor> accessor = findFinalAccessor(currentClass, segments[segments.length - 1]);
        return accessor.map(a -> new NestedPathAccessor(readers, path, a));
    }

    @NotNull
    private static Optional<PropertyAccessor> findFinalAccessor(@NotNull Class<?> currentClass, @NotNull String name) {
        Optional<PropertyAccessor> setter = findSetter(currentClass, name).map(SetterPropertyAccessor::new);

        if (setter.isPresent()) {
            return setter;
        } else {
            return findField(currentClass, name).map(FieldPropertyAccessor::new);
        }
    }

    @NotNull
    private static Optional<Field> findField(@NotNull Class<?> cl, @NotNull String name) {
        Field result = null;

        for (Field field : cl.getFields())
            if (isPublic(field.getModifiers()) && isEqualIgnoringCaseAndUnderscores(name, field.getName()) && !field.isAnnotationPresent(DalesbredIgnore.class)) {
                if (result != null)
                    throw new InstantiationFailureException("Conflicting fields for property: " + result + " - " + name);
                result = field;
            }

        return Optional.ofNullable(result);
    }

    @NotNull
    private static Optional<Method> findSetter(@NotNull Class<?> cl, @NotNull String name) {
        return findGetterOrSetter(cl, name, false);
    }

    @NotNull
    private static Optional<Method> findGetter(@NotNull Class<?> cl, @NotNull String name) {
        return findGetterOrSetter(cl, name, true);
    }

    @NotNull
    private static Optional<Method> findGetterOrSetter(@NotNull Class<?> cl, @NotNull String propertyName, boolean getter) {
        String methodName = (getter ? "get" : "set") + propertyName;
        int parameterCount = getter ? 0 : 1;
        Method result = null;

        for (Method method : cl.getMethods()) {
            if (isPublic(method.getModifiers())
                    && isEqualIgnoringCaseAndUnderscores(methodName, method.getName())
                    && method.getParameterCount() == parameterCount
                    && !method.isAnnotationPresent(DalesbredIgnore.class)) {
                if (result != null)
                    throw new InstantiationFailureException("Conflicting accessors for property: " + result + " - " + propertyName);
                result = method;
            }
        }

        return Optional.ofNullable(result);
    }

    @NotNull
    public static Optional<Type> findPropertyType(@NotNull Class<?> cl, @NotNull String name) {
        return findAccessor(cl, name).map(PropertyAccessor::getType);
    }

    private static final class FieldPropertyAccessor extends PropertyAccessor {

        @NotNull
        private final Field field;

        private FieldPropertyAccessor(@NotNull Field field) {
            this.field = field;
        }

        @Override
        Type getType() {
            return field.getGenericType();
        }

        @Override
        void set(Object object, Object value) {
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw Throwables.propagate(e);
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
        Type getType() {
            return setter.getGenericParameterTypes()[0];
        }

        @Override
        void set(Object object, Object value) {
            try {
                setter.invoke(object, value);
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
    }

    private static final class NestedPathAccessor extends PropertyAccessor {

        @NotNull
        private final PropertyReader[] readers;

        @NotNull
        private final String path;

        @NotNull
        private final PropertyAccessor accessor;

        public NestedPathAccessor(@NotNull PropertyReader[] readers, @NotNull String path, @NotNull PropertyAccessor accessor) {
            this.readers = readers;
            this.path = path;
            this.accessor = accessor;
        }

        @Override
        Type getType() {
            return accessor.getType();
        }

        @Override
        void set(Object object, Object value) {
            accessor.set(resolveFinalObject(object), value);
        }

        @NotNull
        private Object resolveFinalObject(@NotNull Object object) {
            try {
                Object obj = object;

                for (PropertyReader reader : readers) {
                    Object value = reader.propertyValue(obj);
                    if (value != null)
                        obj = value;
                    else
                        throw new InstantiationFailureException(
                                "Failed to set property for '" + path + "', because one of the intermediate objects was null.");
                }

                return obj;

            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new InstantiationFailureException("Failed to set property for '" + path + "'.", e);
            }
        }
    }

    private interface PropertyReader {
        @Nullable
        Object propertyValue(@NotNull Object o) throws IllegalAccessException, InvocationTargetException;
    }
}

