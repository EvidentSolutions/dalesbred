package fi.evident.dalesbred.results;

import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps a single row of result-set into an object.
 */
public interface RowMapper<T> {
    T mapRow(@NotNull ResultSet resultSet) throws SQLException;
}
