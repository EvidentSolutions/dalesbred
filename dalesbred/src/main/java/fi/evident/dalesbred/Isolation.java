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

import java.sql.Connection;

/**
 * Represents the database isolation levels.
 */
public enum Isolation {

    /** Use the default isolation level */
    DEFAULT(0),

    /** @see Connection#TRANSACTION_READ_UNCOMMITTED */
    READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),

    /** @see Connection#TRANSACTION_READ_COMMITTED */
    READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),

    /** @see Connection#TRANSACTION_REPEATABLE_READ */
    REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),

    /** @see Connection#TRANSACTION_SERIALIZABLE */
    SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

    final int level;

    Isolation(int level) {
        this.level = level;
    }

    /**
     * Returns the isolation value for given JDBC code.
     */
    @NotNull
    public static Isolation forJdbcCode(int code) {
        for (Isolation isolation : values())
            if (isolation.level == code)
                return isolation;

        throw new IllegalArgumentException("invalid code: " + code);
    }

    @NotNull
    Isolation normalize(@NotNull Isolation defaultValue) {
        return (this == DEFAULT) ? defaultValue : this;
    }
}
