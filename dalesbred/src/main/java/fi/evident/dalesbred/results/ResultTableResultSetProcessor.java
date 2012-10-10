package fi.evident.dalesbred.results;

import fi.evident.dalesbred.ResultTable;
import fi.evident.dalesbred.ResultTable.ColumnMetadata;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import static fi.evident.dalesbred.utils.ResultSetUtils.getColumnType;
import static java.util.Arrays.asList;

/**
 * Creates a {@link ResultTable} from {@link ResultSet}.
 */
public final class ResultTableResultSetProcessor implements ResultSetProcessor<ResultTable> {

    @NotNull
    @Override
    public ResultTable process(@NotNull ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        ResultTable.Builder builder = createBuilder(metaData);
        while (resultSet.next()) {
            Object[] row = new Object[columnCount];

            for (int i = 0; i < columnCount; i++)
                row[i] = resultSet.getObject(i+1);

            builder.addRow(asList(row));
        }

        return builder.build();
    }

    @NotNull
    private static ResultTable.Builder createBuilder(@NotNull ResultSetMetaData metaData) throws SQLException {
        int columnCount = metaData.getColumnCount();
        ColumnMetadata[] result = new ColumnMetadata[columnCount];



        for (int i = 0; i < columnCount; i++)
            result[i] = new ColumnMetadata(i, metaData.getColumnName(i+1), getColumnType(metaData, i+1), metaData.getColumnType(i+1), metaData.getColumnTypeName(i+1));

        return ResultTable.builder(asList(result));
    }
}
