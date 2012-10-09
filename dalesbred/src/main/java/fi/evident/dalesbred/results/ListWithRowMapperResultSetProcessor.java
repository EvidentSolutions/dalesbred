package fi.evident.dalesbred.results;

import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

/**
 * A ResultSetProcessor that creates a list of results using given RowMapper.
 */
public final class ListWithRowMapperResultSetProcessor<T> implements ResultSetProcessor<List<T>> {
    
    @NotNull
    private final RowMapper<T> rowMapper;
    
    public ListWithRowMapperResultSetProcessor(@NotNull RowMapper<T> rowMapper) {
        this.rowMapper = requireNonNull(rowMapper);
    }

    @Override
    @NotNull
    public List<T> process(@NotNull ResultSet resultSet) throws SQLException {
        List<T> result = new ArrayList<T>();

        while (resultSet.next())
            result.add(rowMapper.mapRow(resultSet));

        return result;
    }
}
