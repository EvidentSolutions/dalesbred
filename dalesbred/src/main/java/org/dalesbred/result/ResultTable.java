/*
 * Copyright (c) 2018 Evident Solutions Oy
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

package org.dalesbred.result;

import org.dalesbred.internal.utils.TypeUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static org.dalesbred.internal.utils.CollectionUtils.mapToList;

/**
 * Represents the results of the query along with its metadata. Basically a detached
 * version of {@link java.sql.ResultSet}.
 */
public final class ResultTable implements Iterable<ResultTable.ResultRow> {

    private final @NotNull List<ColumnMetadata> columns;

    private final @NotNull List<ResultRow> rows;

    private ResultTable(@NotNull List<ColumnMetadata> columns, @NotNull List<ResultRow> rows) {
        this.columns = unmodifiableList(columns);
        this.rows = unmodifiableList(rows);
    }

    public int getRowCount() {
        return rows.size();
    }

    public int getColumnCount() {
        return columns.size();
    }

    /**
     * Returns the value of given column of given row. Both indices are zero-based.
     */
    public Object get(int row, int column) {
        return rows.get(row).get(column);
    }

    /**
     * Returns the value of given named column of given row.
     */
    public Object get(int row, @NotNull String column) {
        return rows.get(row).get(column);
    }

    public @NotNull List<ResultRow> getRows() {
        return rows;
    }

    public @NotNull List<ColumnMetadata> getColumns() {
        return columns;
    }

    public @NotNull List<String> getColumnNames() {
        return mapToList(columns, ColumnMetadata::getName);
    }

    public @NotNull List<Type> getColumnTypes() {
        return mapToList(columns, ColumnMetadata::getType);
    }

    public @NotNull List<Class<?>> getRawColumnTypes() {
        return mapToList(columns, ColumnMetadata::getRawType);
    }

    @Override
    public @NotNull Iterator<ResultRow> iterator() {
        return rows.iterator();
    }

    @Override
    public @NotNull String toString() {
        return "ResultTable [columns=" + columns + ", rows=" + rows.size() + ']';
    }

    /**
     * Returns a formatted representation of this table. See {@link #formatTo(Appendable)} for details of the format.
     *
     * @see #formatTo(Appendable)
     */
    public @NotNull String toStringFormatted() {
        return TableFormatter.toString(this);
    }

    /**
     * Pretty prints this table to {@code out} in a format suitable for console.
     * Overly long columns are truncated and some effort is taken to make the result readable,
     * but you should not depend on the exact details of the layout, since it might change.
     *
     * <p>Hint: the output format is rouhgly that of Markdown tables, so you can use the result in your
     * Markdown-documents. However, data is not escaped, since it's primarily meant to be written
     * to console where escaping would hinder readability. Therefore, you might need to make manual
     * adjustments to output if interpreting it as Markdown.
     *
     * @see #toStringFormatted()
     */
    public void formatTo(@NotNull Appendable out) throws IOException {
        TableFormatter.write(this, out);
    }

    /**
     * Represents a single row of results.
     */
    public static class ResultRow implements Iterable<Object> {

        private final List<Object> values;
        private final ColumnIndices indices;

        private ResultRow(@NotNull List<Object> values, @NotNull ColumnIndices indices) {
            this.values = unmodifiableList(values);
            this.indices = requireNonNull(indices);
        }

        public Object get(int column) {
            return values.get(column);
        }

        public Object get(@NotNull String column) {
            return values.get(indices.columnIndexForName(column));
        }

        @Override
        public @NotNull Iterator<Object> iterator() {
            return values.iterator();
        }

        /**
         * Returns a list containing the values of this row.
         */
        public @NotNull List<Object> asList() {
            return values;
        }

        /**
         * Returns a string representation of this row.
         */
        @Override
        public @NotNull String toString() {
            return values.toString();
        }
    }

    /**
     * Provides metadata about a column of the result.
     */
    public static class ColumnMetadata {
        private final int index;
        private final String name;
        private final Type type;
        private final int jdbcType;
        private final String databaseType;

        public ColumnMetadata(int index, @NotNull String name, @NotNull Type type, int jdbcType, @NotNull String databaseType) {
            this.index = index;
            this.name = requireNonNull(name);
            this.type = requireNonNull(type);
            this.jdbcType = jdbcType;
            this.databaseType = requireNonNull(databaseType);
        }

        /**
         * Returns the zero-based index of this column.
         */
        public int getIndex() {
            return index;
        }

        /**
         * Returns the name of this column.
         */
        public @NotNull String getName() {
            return name;
        }

        /**
         * Returns the Java-type of this column.
         */
        public @NotNull Type getType() {
            return type;
        }

        /**
         * Returns the raw Java-type of this column.
         */
        public @NotNull Class<?> getRawType() {
            return TypeUtils.rawType(type);
        }

        /**
         * Returns the JDBC type code for this column.
         *
         * @see java.sql.Types
         */
        public int getJdbcType() {
            return jdbcType;
        }

        /**
         * Returns the vendor-specific database type name for this column.
         */
        public @NotNull String getDatabaseType() {
            return databaseType;
        }

        @Override
        public @NotNull String toString() {
            return name + ": " + type.getTypeName();
        }
    }

    /**
     * Returns a builder for building a ResultTable for given columns.
     */
    public static @NotNull Builder builder(@NotNull List<ColumnMetadata> columns) {
        return new Builder(columns);
    }

    /**
     * A builder for building ResultTables.
     */
    public static class Builder {

        private final List<ColumnMetadata> columns;
        private final @NotNull ColumnIndices indices;
        private final List<ResultRow> rows = new ArrayList<>();

        public Builder(@NotNull List<ColumnMetadata> columns) {
            this.columns = requireNonNull(columns);
            this.indices = new ColumnIndices(columns);
        }

        public void addRow(@NotNull List<Object> row) {
            if (row.size() != columns.size())
                throw new IllegalArgumentException("expected " + columns + " size values, but got " + row.size());

            rows.add(new ResultRow(row, indices));
        }

        public @NotNull ResultTable build() {
            return new ResultTable(columns, rows);
        }
    }

    /**
     * Data structure to support searches by name.
     */
    private static class ColumnIndices {

        // Just store the names as list. Since the amount of columns is usually small,
        // it's probably faster than HashMap and consumes a lot less memory.
        private final @NotNull String[] names;

        ColumnIndices(@NotNull List<ColumnMetadata> columns) {
            names = new String[columns.size()];

            for (int i = 0; i < names.length; i++)
                names[i] = columns.get(i).getName();
        }

        int columnIndexForName(@NotNull String name) {
            for (int i = 0; i < names.length; i++)
                if (name.equalsIgnoreCase(names[i]))
                    return i;

            throw new IllegalArgumentException("unknown column name '" + name + '\'');
        }
    }
}
