package org.dalesbred.internal.jdbc;

import org.jetbrains.annotations.NotNull;

import java.sql.Array;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

public final class SqlUtils {

    private SqlUtils() { }

    public static void freeArray(@NotNull Array array) throws SQLException {
        try {
            array.free();
        } catch (SQLFeatureNotSupportedException ignored) {
        }
    }
}
