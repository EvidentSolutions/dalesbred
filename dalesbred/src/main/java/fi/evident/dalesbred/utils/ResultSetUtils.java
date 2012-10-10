package fi.evident.dalesbred.utils;

import fi.evident.dalesbred.DatabaseException;
import fi.evident.dalesbred.instantiation.NamedTypeList;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public final class ResultSetUtils {

    private ResultSetUtils() { }

    @NotNull
    public static NamedTypeList getTypes(@NotNull ResultSetMetaData metaData) throws SQLException {
        int columns = metaData.getColumnCount();

        NamedTypeList.Builder result = NamedTypeList.builder(columns);

        for (int i = 0; i < columns; i++)
            result.add(metaData.getColumnName(i+1), ResultSetUtils.getColumnType(metaData, i+1));

        return result.build();
    }

    @NotNull
    public static Class<?> getColumnType(@NotNull ResultSetMetaData metaData, int column) throws SQLException {
        String className = metaData.getColumnClassName(column);
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("Could not find class '" + className + "'", e);
        }
    }
}
