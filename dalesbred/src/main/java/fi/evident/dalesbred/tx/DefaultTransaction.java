/*
 * Copyright (c) 2013 Evident Solutions Oy
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
package fi.evident.dalesbred.tx;

import fi.evident.dalesbred.Isolation;
import fi.evident.dalesbred.TransactionCallback;
import fi.evident.dalesbred.TransactionContext;
import fi.evident.dalesbred.TransactionSerializationException;
import fi.evident.dalesbred.connection.ConnectionProvider;
import fi.evident.dalesbred.dialects.Dialect;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.logging.Level;
import java.util.logging.Logger;

import static fi.evident.dalesbred.utils.Require.requireNonNull;
import static fi.evident.dalesbred.utils.Throwables.propagate;

final class DefaultTransaction {

    @NotNull
    private final Connection connection;

    @NotNull
    private final ConnectionProvider connectionProvider;

    @NotNull
    private final Dialect dialect;
    private static final Logger log = Logger.getLogger(DefaultTransaction.class.getName());

    @SuppressWarnings("JDBCResourceOpenedButNotSafelyClosed")
    DefaultTransaction(@NotNull ConnectionProvider connectionProvider, @NotNull Dialect dialect, @NotNull Isolation isolation) {
        try {
            this.connectionProvider = requireNonNull(connectionProvider);
            this.connection = connectionProvider.getConnection();
            this.dialect = requireNonNull(dialect);

            connection.setAutoCommit(false);

            if (isolation != Isolation.DEFAULT)
                connection.setTransactionIsolation(isolation.getJdbcLevel());

        } catch (SQLException e) {
            throw dialect.convertException(e);
        }
    }

    <T> T execute(int retries, @NotNull TransactionCallback<T> callback) {
        int tries = 1;
        while (true) {
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
                    log.log(Level.WARNING, "rolled back transaction because of exception: " + e, e);
                    throw propagate(e, SQLException.class);
                }
            } catch (TransactionSerializationException e) {
                if (tries++ > retries)
                    throw e;

                log.fine("automatically retrying failed transaction");
            } catch (SQLException e) {
                throw dialect.convertException(e);
            }
        }
    }

    <T> T nested(int retries, @NotNull TransactionCallback<T> callback) {
        int tries = 1;
        while (true) {
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
                    log.log(Level.WARNING, "rolled back nested transaction because of exception: " + e, e);
                    throw propagate(e, SQLException.class);
                }
            } catch (TransactionSerializationException e) {
                if (tries++ > retries)
                    throw e;

                log.fine("automatically retrying failed transaction");
            } catch (SQLException e) {
                throw dialect.convertException(e);
            }
        }
    }

    <T> T join(@NotNull TransactionCallback<T> callback) {
        try {
            return callback.execute(new DefaultTransactionContext(connection));
        } catch (SQLException e) {
            throw dialect.convertException(e);
        }
    }

    void close() {
        try {
            connectionProvider.releaseConnection(connection);
        } catch (SQLException e) {
            throw dialect.convertException(e);
        }
    }
}
