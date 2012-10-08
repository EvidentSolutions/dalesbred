package fi.evident.dalesbred;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Callback for operations that execute code within a context of a {@link Connection}.
 *
 * @see Database#withTransaction(ConnectionCallback)
 */
public interface ConnectionCallback<T> {
    T execute(@NotNull Connection connection) throws SQLException;
}
