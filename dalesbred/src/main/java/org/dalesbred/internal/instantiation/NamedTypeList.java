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

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

/**
 * Represents a named list of types, e.g. the result types of SQL-query.
 */
public final class NamedTypeList {

    @NotNull
    private final List<String> names;

    @NotNull
    private final List<Type> types;

    private NamedTypeList(@NotNull List<String> names, @NotNull List<Type> types) {
        assert names.size() == types.size();
        this.names = unmodifiableList(names);
        this.types = types;
    }

    public int size() {
        return types.size();
    }

    @NotNull
    public String getName(int index) {
        return names.get(index);
    }

    @NotNull
    public Type getType(int index) {
        return types.get(index);
    }

    @NotNull
    public List<String> getNames() {
        return names;
    }

    @NotNull
    public NamedTypeList subList(int fromIndex, int toIndex) {
        return new NamedTypeList(names.subList(fromIndex, toIndex), types.subList(fromIndex, toIndex));
    }

    @Override
    @NotNull
    public String toString() {
        int size = types.size();

        @SuppressWarnings("MagicNumber")
        StringBuilder sb = new StringBuilder(10 + size * 30);

        sb.append('[');

        for (int i = 0; i < size; i++) {
            if (i != 0) sb.append(", ");

            sb.append(names.get(i)).append(": ").append(types.get(i).getTypeName());
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

        private boolean built = false;

        @NotNull
        private final List<String> names;

        @NotNull
        private final List<Type> types;

        private Builder(int size) {
            this.names = new ArrayList<>(size);
            this.types = new ArrayList<>(size);
        }

        public Builder add(@NotNull String name, @NotNull Type type) {
            if (built) throw new IllegalStateException("can't add items to builder that has been built");

            names.add(requireNonNull(name));
            types.add(requireNonNull(type));
            return this;
        }

        @NotNull
        public NamedTypeList build() {
            built = true;
            return new NamedTypeList(names, types);
        }
    }
}
