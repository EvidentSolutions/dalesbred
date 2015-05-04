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

import org.dalesbred.NonUniqueResultException;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * A {@link ResultSetProcessor} that adapts another a list-producing processor to produce optional result.
 * Note that a single null result will produce {@code Optional.empty()} because it's not possible to represent
 * {@code Optional.of(null)} and because the pattern of single null return meaning null comes up quite often
 * when dealing with aggregate queries (e.g. "select max(salary) from employee").
 */
public final class OptionalResultSetProcessor<T> implements ResultSetProcessor<Optional<T>> {

    @NotNull
    private final ResultSetProcessor<List<T>> resultSetProcessor;

    public OptionalResultSetProcessor(@NotNull ResultSetProcessor<List<T>> resultSetProcessor) {
        this.resultSetProcessor = resultSetProcessor;
    }

    @Override
    @NotNull
    public Optional<T> process(@NotNull ResultSet resultSet) throws SQLException {
        List<T> results = resultSetProcessor.process(resultSet);

        if (results.isEmpty())
            return Optional.empty();
        else if (results.size() == 1)
            return Optional.ofNullable(results.get(0));
        else
            throw new NonUniqueResultException(results.size());
    }
}
