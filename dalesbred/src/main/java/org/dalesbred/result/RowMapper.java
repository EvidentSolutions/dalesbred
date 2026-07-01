package org.dalesbred.result;

import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Maps a single row of result-set into an object.
 */
@FunctionalInterface
public interface RowMapper<T> {

    /**
     * Produces a single value based on current row.
     * <p>
     * The implementation should not call {@link ResultSet#next()} or other methods to move
     * the current position of the {@link ResultSet}, caller is responsible for that.
     */
    T mapRow(@NotNull ResultSet resultSet) throws SQLException;

    /**
     * Creates a {@link ResultSetProcessor} that applies this row-mapper to every row
     * and results a list.
     */
    default @NotNull ResultSetProcessor<List<T>> list() {
        return resultSet -> {
            List<T> result = new ArrayList<>();

            while (resultSet.next())
                result.add(mapRow(resultSet));

            return result;
        };
    }

    /**
     * Creates a {@link ResultSetProcessor} that expects a single result row from database.
     */
    default @NotNull ResultSetProcessor<T> unique() {
        return resultSet -> {
            if (!resultSet.next())
                throw new EmptyResultException();

            T result = mapRow(resultSet);

            if (resultSet.next())
                throw new NonUniqueResultException();

            return result;
        };
    }

    /**
     * Creates a {@link ResultSetProcessor} that expects zero or one result row from the database.
     */
    default @NotNull ResultSetProcessor<Optional<T>> optional() {
        return resultSet -> {
            if (!resultSet.next())
                return Optional.empty();

            Optional<T> result = Optional.ofNullable(mapRow(resultSet));

            if (resultSet.next())
                throw new NonUniqueResultException();

            return result;
        };
    }
}
