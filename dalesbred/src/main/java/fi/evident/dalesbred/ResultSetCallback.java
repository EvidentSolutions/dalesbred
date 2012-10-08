package fi.evident.dalesbred;

import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Callback for processing a {@link java.sql.ResultSet}.
 */
public interface ResultSetCallback<T> {
    T execute(@NotNull ResultSet resultSet) throws SQLException;
}
