package fi.evident.dalesbred.results;

import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps a single row of result-set into an object.
 */
public interface RowMapper<T> {

    /**
     * Produces a single value based on current row.
     * <p>
     * The implementation should not call {@link ResultSet#next()} or other methods to move
     * the current position of the {@link ResultSet}, caller is responsible for that.
     *
     * @throws SQLException
     */
    T mapRow(@NotNull ResultSet resultSet) throws SQLException;
}
