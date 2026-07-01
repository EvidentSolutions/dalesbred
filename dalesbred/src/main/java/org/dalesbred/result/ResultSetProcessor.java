package org.dalesbred.result;

import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Callback for processing a whole {@link ResultSet}.
 */
@FunctionalInterface
public interface ResultSetProcessor<T> {
    T process(@NotNull ResultSet resultSet) throws SQLException;
}
