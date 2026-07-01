package org.dalesbred.internal.jdbc;

import org.dalesbred.DatabaseException;
import org.dalesbred.dialect.Dialect;
import org.dalesbred.internal.instantiation.NamedTypeList;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Utilities for processing {@link java.sql.ResultSet}s.
 */
public final class ResultSetUtils {

    private ResultSetUtils() { }

    public static @NotNull NamedTypeList getTypes(@NotNull ResultSetMetaData metaData, @NotNull Dialect dialect) throws SQLException {
        int columns = metaData.getColumnCount();

        NamedTypeList.Builder result = NamedTypeList.builder(columns);

        for (int i = 0; i < columns; i++)
            result.add(metaData.getColumnLabel(i+1), getColumnType(metaData, dialect, i + 1));

        return result.build();
    }

    public static @NotNull Type getColumnType(@NotNull ResultSetMetaData metaData, @NotNull Dialect dialect, int column) throws SQLException {
        String className = metaData.getColumnClassName(column);

        Type overrideType = dialect.overrideResultSetMetaDataType(className);
        if (overrideType != null)
            return overrideType;

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("Could not find class '" + className + "' specified by ResultSet.", e);
        }
    }
}
