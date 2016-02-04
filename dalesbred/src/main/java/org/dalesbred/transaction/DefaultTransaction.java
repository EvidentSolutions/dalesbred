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

package org.dalesbred.transaction;

import org.dalesbred.dialect.Dialect;
import org.dalesbred.internal.utils.Throwables;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

import static java.util.Objects.requireNonNull;

final class DefaultTransaction {

    @NotNull
    private final Connection connection;

    @NotNull
    private static final Logger log = LoggerFactory.getLogger(DefaultTransaction.class);

    DefaultTransaction(@NotNull Connection connection) {
        this.connection = requireNonNull(connection);
    }

    <T> T execute(@NotNull TransactionCallback<T> callback, @NotNull Dialect dialect) {
        try {
            try {
                TransactionContext ctx = new DefaultTransactionContext(connection);
                T value = callback.execute(ctx);
                if (ctx.isRollbackOnly())
                    connection.rollback();
                else
                    connection.commit();
                return value;

            } catch (Exception e) {
                connection.rollback();
                log.warn("rolled back transaction because of exception.", e);
                throw Throwables.propagate(e, SQLException.class);
            }
        } catch (SQLException e) {
            throw dialect.convertException(e);
        }
    }

    <T> T nested(@NotNull TransactionCallback<T> callback, @NotNull Dialect dialect) {
        try {
            Savepoint savepoint = connection.setSavepoint();
            try {
                TransactionContext ctx = new DefaultTransactionContext(connection);
                T value = callback.execute(ctx);
                if (ctx.isRollbackOnly())
                    connection.rollback(savepoint);
                else
                    connection.releaseSavepoint(savepoint);
                return value;

            } catch (Exception e) {
                connection.rollback(savepoint);
                log.warn("rolled back nested transaction because of exception.", e);
                throw Throwables.propagate(e, SQLException.class);
            }
        } catch (SQLException e) {
            throw dialect.convertException(e);
        }
    }

    <T> T join(@NotNull TransactionCallback<T> callback, @NotNull Dialect dialect) {
        try {
            return callback.execute(new DefaultTransactionContext(connection));
        } catch (SQLException e) {
            throw dialect.convertException(e);
        }
    }
}
