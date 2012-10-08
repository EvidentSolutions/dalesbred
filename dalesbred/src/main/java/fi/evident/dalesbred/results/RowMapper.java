package fi.evident.dalesbred.results;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps a single row of result-set into an object.
 */
public interface RowMapper<T> {
    T mapRow(ResultSet resultSet) throws SQLException;
}
