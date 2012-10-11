package fi.evident.dalesbred;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Callback for operations that execute code within a context of a {@link java.sql.Connection}.
 *
 * @see Database#withTransaction(TransactionCallback)
 */
public interface TransactionCallback<T> {
    T execute(@NotNull Connection connection) throws SQLException;
}
