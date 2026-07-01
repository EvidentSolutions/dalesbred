package org.dalesbred.query;

import org.dalesbred.DatabaseException;
import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when parsing SQL fails.
 */
public class SqlSyntaxException extends DatabaseException {

    public SqlSyntaxException(@NotNull String message, @NotNull String sql) {
        super(message + " [" + sql + ']');
    }
}
