/*
 * Copyright (c) 2015 Evident Solutions Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.dalesbred.result;

import org.dalesbred.EmptyResultException;
import org.dalesbred.NonUniqueResultException;
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
     *
     * @throws SQLException
     */
    T mapRow(@NotNull ResultSet resultSet) throws SQLException;

    /**
     * Creates a {@link ResultSetProcessor} that applies this row-mapper to every row
     * and results a list.
     */
    @NotNull
    default ResultSetProcessor<List<T>> list() {
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
    @NotNull
    default ResultSetProcessor<T> unique() {
        return resultSet -> {
            List<T> results = list().process(resultSet);

            if (results.size() == 1)
                return results.get(0);
            else if (results.isEmpty())
                throw new EmptyResultException();
            else
                throw new NonUniqueResultException();
        };
    }

    /**
     * Creates a {@link ResultSetProcessor} that no rows or single row from database.
     */
    @NotNull
    default ResultSetProcessor<Optional<T>> optional() {
        return resultSet -> {
            List<T> results = list().process(resultSet);

            if (results.size() == 1)
                return Optional.ofNullable(results.get(0));
            else if (results.isEmpty())
                return Optional.empty();
            else
                throw new NonUniqueResultException();
        };
    }
}
