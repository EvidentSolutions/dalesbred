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

import java.lang.reflect.Constructor;
import java.util.List;

import static fi.evident.dalesbred.utils.Require.requireNonNull;
import static fi.evident.dalesbred.utils.Throwables.propagate;

final class ConstructorInstantiator<T> implements Instantiator<T> {

    @NotNull
    private final Constructor<T> constructor;

    @NotNull
    private final List<TypeConversion<Object,?>> coercions;

    ConstructorInstantiator(@NotNull Constructor<T> constructor, @NotNull List<TypeConversion<Object,?>> coercions) {
        this.constructor = requireNonNull(constructor);
        this.coercions = requireNonNull(coercions);
    }

    @Override
    @NotNull
    public T instantiate(@NotNull Object[] arguments) {
        try {
            return constructor.newInstance(coerceArguments(arguments));
        } catch (Exception e) {
            throw propagate(e);
        }
    }

    @NotNull
    private Object[] coerceArguments(@NotNull Object[] arguments) {
        Object[] coerced = new Object[arguments.length];

        for (int i = 0; i < arguments.length; i++)
            coerced[i] = coercions.get(i).convert(arguments[i]);

        return coerced;
    }
}
