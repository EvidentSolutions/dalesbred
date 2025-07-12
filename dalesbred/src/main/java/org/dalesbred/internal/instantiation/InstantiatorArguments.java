/*
 * Copyright (c) 2017 Evident Solutions Oy
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

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * Contains the arguments of instantiator as well their names and types.
 */
public final class InstantiatorArguments {

    private final @NotNull NamedTypeList types;

    private final @NotNull List<?> values;

    public InstantiatorArguments(@NotNull NamedTypeList types, @NotNull Object[] values) {
        this(types, Arrays.asList(values));
    }

    public InstantiatorArguments(@NotNull NamedTypeList types, @NotNull List<?> values) {
        if (types.size() != values.size())
            throw new IllegalArgumentException("got " + types.size() + " types, but " + values.size() + " values");

        this.types = types;
        this.values = unmodifiableList(values);
    }

    public @NotNull NamedTypeList getTypes() {
        return types;
    }

    public @NotNull List<?> getValues() {
        return values;
    }

    public int size() {
        return types.size();
    }

    public Object getSingleValue() {
        if (values.size() != 1)
            throw new IllegalStateException("expected single argument, but got " + values.size());

        return values.getFirst();
    }
}
