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
