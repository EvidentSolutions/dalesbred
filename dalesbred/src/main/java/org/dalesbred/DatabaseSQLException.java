package org.dalesbred;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

/**
 * Wraps an {@link SQLException} originating from database.
 */
public class DatabaseSQLException extends DatabaseException {

    public DatabaseSQLException(@NotNull String message, @NotNull SQLException cause) {
        super(message, cause);
    }

    public DatabaseSQLException(@NotNull SQLException cause) {
        super(cause);
    }

    @Override
    public synchronized @NotNull SQLException getCause() {
        return (SQLException) super.getCause();
    }
}
