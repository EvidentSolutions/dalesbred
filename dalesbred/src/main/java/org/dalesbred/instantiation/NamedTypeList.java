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

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.dalesbred.utils.Require.requireNonNull;

/**
 * Represents a named list of types, e.g. the result types of SQL-query.
 */
public final class NamedTypeList {

    @NotNull
    private final String[] names;

    @NotNull
    private final Class<?>[] types;

    private NamedTypeList(@NotNull String[] names, @NotNull Class<?>[] types) {
        this.names = names;
        this.types = types;
    }

    public int size() {
        return types.length;
    }

    @NotNull
    public String getName(int index) {
        return names[index];
    }

    @NotNull
    public Class<?> getType(int index) {
        return types[index];
    }

    @NotNull
    public List<String> getNames() {
        return unmodifiableList(asList(names));
    }

    @Override
    @NotNull
    public String toString() {
        @SuppressWarnings("MagicNumber")
        StringBuilder sb = new StringBuilder(10 + types.length * 30);

        sb.append('[');

        for (int i = 0; i < types.length; i++) {
            if (i != 0) sb.append(", ");

            sb.append(names[i]).append(": ").append(types[i].getName());
        }

        sb.append(']');

        return sb.toString();
    }

    @NotNull
    public static Builder builder(int size) {
        return new Builder(size);
    }

    /**
     * Builder for {@link NamedTypeList}s.
     */
    public static class Builder {
        private int index = 0;

        @NotNull
        private final String[] names;
        @NotNull
        private final Class<?>[] types;

        private Builder(int size) {
            this.names = new String[size];
            this.types = new Class<?>[size];
        }

        public Builder add(@NotNull String name, @NotNull Class<?> type) {
            names[index] = requireNonNull(name);
            types[index] = requireNonNull(type);
            index++;
            return this;
        }

        @NotNull
        public NamedTypeList build() {
            if (index != names.length)
                throw new IllegalStateException("expected " + names.length + " items, but got only " + index);

            index = -1; // set the index to invalid value so that we can no longer modify the class
            return new NamedTypeList(names, types);
        }
    }
}
