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

package org.dalesbred.instantiation;

import org.dalesbred.internal.utils.Throwables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * An instantiator that uses constructor and setters or fields to instantiate an object.
 */
final class ReflectionInstantiator<T> implements Instantiator<T> {

    @NotNull
    private final Constructor<T> constructor;

    @NotNull
    private final TypeConversion<Object, ?>[] conversions;

    @NotNull
    private final PropertyAccessor[] accessors;

    @Nullable
    private final InstantiationListener instantiationListener;

    private final int constructorParameterCount;

    ReflectionInstantiator(@NotNull Constructor<T> constructor,
                           @NotNull TypeConversion<Object, ?>[] conversions,
                           @NotNull PropertyAccessor[] accessors,
                           @Nullable InstantiationListener instantiationListener) {
        this.constructor = requireNonNull(constructor);
        this.conversions = requireNonNull(conversions);
        this.accessors = requireNonNull(accessors);
        this.instantiationListener = instantiationListener;
        this.constructorParameterCount = constructor.getParameterTypes().length;
    }

    @Override
    @NotNull
    public T instantiate(@NotNull InstantiatorArguments arguments) {
        try {
            T value = constructor.newInstance(constructorArguments(arguments.getValues()));
            bindRemainingProperties(value, arguments);
            if (instantiationListener != null)
                instantiationListener.onInstantiation(value);
            return value;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private void bindRemainingProperties(@NotNull T result, @NotNull InstantiatorArguments arguments) {
        for (int i = 0; i < accessors.length; i++) {
            int argumentIndex = i + constructorParameterCount;
            Object originalValue = arguments.getValues().get(argumentIndex);
            Object convertedValue = convert(originalValue, conversions[argumentIndex]);
            accessors[i].set(result, convertedValue);
        }
    }

    @NotNull
    private Object[] constructorArguments(@NotNull List<?> arguments) {
        Object[] result = new Object[constructorParameterCount];

        for (int i = 0; i < result.length; i++)
            result[i] = convert(arguments.get(i), conversions[i]);

        return result;
    }

    @Nullable
    private static Object convert(@Nullable Object value, @NotNull TypeConversion<Object, ?> conversion) {
        if (value != null)
            return conversion.convert(value);
        else
            return null;
    }
}
