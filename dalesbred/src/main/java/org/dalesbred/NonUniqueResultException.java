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

package org.dalesbred;

import org.dalesbred.query.SqlQuery;
import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when expecting a unique result from a call, but more then one row
 * of results was returned by the database.
 *
 * @see Database#findUnique(Class, SqlQuery)
 * @see Database#findOptional(Class, SqlQuery)
 * @see Database#findUniqueOrNull(Class, SqlQuery)
 * @see EmptyResultException
 */
public class NonUniqueResultException extends UnexpectedResultException {

    public NonUniqueResultException() {
        super("Expected unique result but received more than one row");
    }

    protected NonUniqueResultException(@NotNull String message) {
        super(message);
    }
}
