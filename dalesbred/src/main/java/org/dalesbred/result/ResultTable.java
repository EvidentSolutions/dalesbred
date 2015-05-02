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

package org.dalesbred.result;

import org.jetbrains.annotations.NotNull;

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

    @NotNull
    private final List<ColumnMetadata> columns;

    @NotNull
    private final List<ResultRow> rows;

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

    @NotNull
    public List<ResultRow> getRows() {
        return rows;
    }

    @NotNull
    public List<ColumnMetadata> getColumns() {
        return columns;
    }

    @NotNull
    public List<String> getColumnNames() {
        return mapToList(columns, ColumnMetadata::getName);
    }

    @NotNull
    public List<Class<?>> getColumnTypes() {
        return mapToList(columns, ColumnMetadata::getType);
    }

    @Override
    @NotNull
    public Iterator<ResultRow> iterator() {
        return rows.iterator();
    }

    @Override
    @NotNull
    public String toString() {
        return "ResultTable [columns=" + columns + ", rows=" + rows.size() + ']';
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
        @NotNull
        public Iterator<Object> iterator() {
            return values.iterator();
        }

        /**
         * Returns a list containing the values of this row.
         */
        @NotNull
        public List<Object> asList() {
            return values;
        }

        /**
         * Returns a string representation of this row.
         */
        @Override
        @NotNull
        public String toString() {
            return values.toString();
        }
    }

    /**
     * Provides metadata about a column of the result.
     */
    public static class ColumnMetadata {
        private final int index;
        private final String name;
        private final Class<?> type;
        private final int jdbcType;
        private final String databaseType;

        public ColumnMetadata(int index, @NotNull String name, @NotNull Class<?> type, int jdbcType, @NotNull String databaseType) {
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
        @NotNull
        public String getName() {
            return name;
        }

        /**
         * Returns the Java-type of this column.
         */
        @NotNull
        public Class<?> getType() {
            return type;
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
        @NotNull
        public String getDatabaseType() {
            return databaseType;
        }

        @NotNull
        @Override
        public String toString() {
            return name + ": " + type.getName();
        }
    }

    /**
     * Returns a builder for building a ResultTable for given columns.
     */
    @NotNull
    public static Builder builder(@NotNull List<ColumnMetadata> columns) {
        return new Builder(columns);
    }

    /**
     * A builder for building ResultTables.
     */
    public static class Builder {

        private final List<ColumnMetadata> columns;
        @NotNull
        private final ColumnIndices indices;
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

        @NotNull
        public ResultTable build() {
            return new ResultTable(columns, rows);
        }
    }

    /**
     * Data structure to support searches by name.
     */
    private static class ColumnIndices {

        // Just store the names as list. Since the amount of columns is usually small,
        // it's probably faster than HashMap and consumes a lot less memory.
        @NotNull
        private final String[] names;

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
