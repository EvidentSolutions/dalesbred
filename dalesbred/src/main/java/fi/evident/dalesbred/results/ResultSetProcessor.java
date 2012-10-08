package fi.evident.dalesbred.results;

import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Callback for processing a whole {@link java.sql.ResultSet}.
 */
public interface ResultSetProcessor<T> {
    T process(@NotNull ResultSet resultSet) throws SQLException;
}
