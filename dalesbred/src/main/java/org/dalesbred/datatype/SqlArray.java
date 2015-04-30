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

package org.dalesbred.datatype;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

/**
 * Wrapper for values that are to be bound as {@link java.sql.Array} objects
 * when executing queries.
 */
public final class SqlArray {

    /** Database specific type name of the array */
    @NotNull
    private final String type;

    /** Values for the array */
    @NotNull
    private final List<?> values;

    private SqlArray(@NotNull String type, @NotNull List<?> values) {
        this.type = requireNonNull(type);
        this.values = unmodifiableList(new ArrayList<>(values));
    }

    /**
     * Constructs array of specified type.
     *
     * @param type database type for the array
     * @param values for the array
     */
    @NotNull
    public static SqlArray of(@NotNull String type, @NotNull List<?> values) {
        return new SqlArray(type, values);
    }

    /**
     * Constructs array of specified type.
     *
     * @param type database type for the array
     * @param values for the array
     */
    @NotNull
    public static SqlArray of(@NotNull String type, @NotNull Object[] values) {
        return of(type, asList(values));
    }

    /**
     * Constructs varchar array of given values.
     */
    @NotNull
    public static SqlArray varchars(@NotNull List<String> values) {
        return of("varchar", values);
    }

    /**
     * Constructs varchar array of given values.
     */
    @NotNull
    public static SqlArray varchars(@NotNull String... values) {
        return varchars(asList(values));
    }

    /**
     * Returns the database type for the array.
     */
    @NotNull
    public String getType() {
        return type;
    }

    /**
     * Returns the values of the array.
     */
    @NotNull
    public List<?> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "SQLArray[type=" + type + ", values=" + values + ']';
    }
}
