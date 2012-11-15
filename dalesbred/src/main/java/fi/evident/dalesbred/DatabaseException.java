/*
 * Copyright (c) 2012 Evident Solutions Oy
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

package fi.evident.dalesbred;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for all of Dalesbred's exceptions.
 */
public class DatabaseException extends RuntimeException {

    @Nullable
    private final SqlQuery query = DebugContext.getCurrentQuery();

    public DatabaseException(@NotNull String message) {
        super(message);
    }

    public DatabaseException(@NotNull Throwable cause) {
        super(cause);
    }
    
    public DatabaseException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }

    /**
     * If this exception was thrown during an execution of a query, returns the query. Otherwise
     * returns {@code null}.
     */
    @Nullable
    public SqlQuery getQuery() {
        return query;
    }

    @Override
    public String toString() {
        String basicToString = super.toString();
        if (query != null)
            return basicToString + " (query: " + query + ')';
        else
            return basicToString;
    }
}
