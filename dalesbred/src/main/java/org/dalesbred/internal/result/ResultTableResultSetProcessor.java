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

package org.dalesbred.internal.result;

import org.dalesbred.dialect.Dialect;
import org.dalesbred.result.ResultSetProcessor;
import org.dalesbred.result.ResultTable;
import org.dalesbred.result.ResultTable.ColumnMetadata;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.dalesbred.internal.jdbc.ResultSetUtils.getColumnType;

/**
 * Creates a {@link ResultTable} from {@link ResultSet}.
 */
public final class ResultTableResultSetProcessor implements ResultSetProcessor<ResultTable> {

    private final @NotNull Dialect dialect;

    public ResultTableResultSetProcessor(@NotNull Dialect dialect) {
        this.dialect = requireNonNull(dialect);
    }

    @Override
    public @NotNull ResultTable process(@NotNull ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        ResultTable.Builder builder = createBuilder(metaData, dialect);
        while (resultSet.next()) {
            Object[] row = new Object[columnCount];

            for (int i = 0; i < columnCount; i++)
                row[i] = resultSet.getObject(i+1);

            builder.addRow(asList(row));
        }

        return builder.build();
    }

    private static @NotNull ResultTable.Builder createBuilder(@NotNull ResultSetMetaData metaData, @NotNull Dialect dialect) throws SQLException {
        int columnCount = metaData.getColumnCount();
        ColumnMetadata[] result = new ColumnMetadata[columnCount];

        for (int i = 0; i < columnCount; i++) {
            result[i] = new ColumnMetadata(i, metaData.getColumnLabel(i + 1), getColumnType(metaData, dialect, i + 1),
                metaData.getColumnType(i + 1), metaData.getColumnTypeName(i + 1));
        }

        return ResultTable.builder(asList(result));
    }
}
