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

import java.lang.reflect.Type;
import java.util.function.Function;

final class OptionalConversion<S,T,O> extends TypeConversion<S, O> {

    @NotNull
    private final TypeConversion<S,T> conversion;

    @NotNull
    private final Function<T,O> nonEmptyBuilder;

    @NotNull
    private final O emptyValue;

    OptionalConversion(@NotNull Type source,
                       @NotNull Type target,
                       @NotNull TypeConversion<S,T> conversion,
                       @NotNull Function<T, O> nonEmptyBuilder,
                       @NotNull O emptyValue) {
        super(source, target);
        this.conversion = conversion;
        this.nonEmptyBuilder = nonEmptyBuilder;
        this.emptyValue = emptyValue;
    }

    @NotNull
    @Override
    public O convert(@Nullable S value) {
        T convert = conversion.convert(value);
        if (value != null)
            return nonEmptyBuilder.apply(convert);
        else
            return emptyValue;
    }
}
