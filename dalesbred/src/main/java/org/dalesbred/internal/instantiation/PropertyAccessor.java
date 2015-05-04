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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

import static java.lang.reflect.Modifier.isPublic;
import static org.dalesbred.internal.utils.StringUtils.isEqualIgnoringCaseAndUnderscores;

abstract class PropertyAccessor {

    abstract void set(Object object, Object value);

    abstract Type getType();

    @NotNull
    static Optional<PropertyAccessor> findAccessor(@NotNull Class<?> cl, @NotNull String name) {
        Optional<PropertyAccessor> setter = findSetter(cl, name).map(SetterPropertyAccessor::new);

        if (setter.isPresent()) {
            return setter;
        } else {
            return findField(cl, name).map(FieldPropertyAccessor::new);
        }
    }

    @NotNull
    private static Optional<Field> findField(@NotNull Class<?> cl, @NotNull String name) {
        Field result = null;

        for (Field field : cl.getFields())
            if (isPublic(field.getModifiers()) && isEqualIgnoringCaseAndUnderscores(name, field.getName()) && !field.isAnnotationPresent(DalesbredIgnore.class)) {
                if (result != null)
                    throw new InstantiationException("Conflicting fields for property: " + result + " - " + name);
                result = field;
            }

        return Optional.ofNullable(result);
    }

    @NotNull
    private static Optional<Method> findSetter(@NotNull Class<?> cl, @NotNull String name) {
        Method result = null;

        String methodName = "set" + name;
        for (Method method : cl.getMethods()) {

            if (isPublic(method.getModifiers()) && isEqualIgnoringCaseAndUnderscores(methodName, method.getName()) && method.getParameterTypes().length == 1 && !method.isAnnotationPresent(DalesbredIgnore.class)) {
                if (result != null)
                    throw new InstantiationException("Conflicting setters for property: " + result + " - " + name);
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
}

