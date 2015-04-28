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

package org.dalesbred.results;

import org.dalesbred.NonUniqueResultException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * A {@link ResultSetProcessor} that adapts another a list-producing processor to produce only single result.
 */
public final class UniqueResultSetProcessor<T> implements ResultSetProcessor<T> {

    @NotNull
    private final ResultSetProcessor<List<T>> resultSetProcessor;

    private final boolean allowEmptyResult;

    @NotNull
    public static <T> ResultSetProcessor<T> unique(@NotNull ResultSetProcessor<List<T>> processor) {
        return new UniqueResultSetProcessor<T>(processor, false);
    }

    @NotNull
    public static <T> ResultSetProcessor<T> uniqueOrEmpty(@NotNull ResultSetProcessor<List<T>> processor) {
        return new UniqueResultSetProcessor<T>(processor, true);
    }

    private UniqueResultSetProcessor(@NotNull ResultSetProcessor<List<T>> resultSetProcessor, boolean allowEmptyResult) {
        this.resultSetProcessor = resultSetProcessor;
        this.allowEmptyResult = allowEmptyResult;
    }

    @Override
    @Nullable
    public T process(@NotNull ResultSet resultSet) throws SQLException {
        List<T> results = resultSetProcessor.process(resultSet);

        if (results.isEmpty() && allowEmptyResult)
            return null;
        else if (results.size() == 1)
            return results.get(0);
        else
            throw new NonUniqueResultException(results.size());
    }
}
