package fi.evident.dalesbred;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

/**
 * Callback for executing JDBC-operations in given context.
 */
public interface JdbcCallback<C,T> {
    T execute(@NotNull C context) throws SQLException;
}
