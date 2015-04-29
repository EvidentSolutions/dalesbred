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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.dalesbred.internal.utils.Require.requireNonNull;

/**
 * A simple instantiator that just applies a coercion to argument.
 */
final class CoercionInstantiator<T> implements Instantiator<T> {

    @NotNull
    private final TypeConversion<Object, ? extends T> coercion;

    @Nullable
    private final InstantiationListener instantiationListener;

    CoercionInstantiator(@NotNull TypeConversion<Object, ? extends T> coercion,
                         @Nullable InstantiationListener instantiationListener) {
        this.coercion = requireNonNull(coercion);
        this.instantiationListener = instantiationListener;
    }

    @Nullable
    @Override
    public T instantiate(@NotNull InstantiatorArguments arguments) {
        assert arguments.getValues().size() == 1;

        Object value = arguments.getValues().get(0);
        if (value != null) {
            T result = coercion.convert(value);
            if (instantiationListener != null)
                instantiationListener.onInstantiation(result);
            return result;
        } else
            return null;
    }
}
