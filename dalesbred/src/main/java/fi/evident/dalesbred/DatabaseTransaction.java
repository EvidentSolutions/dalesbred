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

import fi.evident.dalesbred.dialects.Dialect;
import org.jetbrains.annotations.NotNull;

import javax.inject.Provider;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.logging.Level;
import java.util.logging.Logger;

import static fi.evident.dalesbred.utils.Require.requireNonNull;
import static fi.evident.dalesbred.utils.Throwables.propagate;

/**
 * Represents the active database-transaction.
 */
final class DatabaseTransaction {

    private final Connection connection;
    private final Dialect dialect;
    private static final Logger log = Logger.getLogger(DatabaseTransaction.class.getName());

    DatabaseTransaction(@NotNull Provider<Connection> connectionProvider, @NotNull Dialect dialect, Isolation isolation) {
        this.connection = requireNonNull(connectionProvider.get(), "null connection from connection-provider");
        this.dialect = requireNonNull(dialect);

        try {
            connection.setAutoCommit(false);

            if (isolation != null)
                connection.setTransactionIsolation(isolation.level);

        } catch (SQLException e) {
            throw dialect.convertException(e);
        }
    }

    <T> T execute(@NotNull final TransactionCallback<T> callback) {
        try {
            try {
                TransactionContext ctx = new TransactionContext(connection);
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
        } catch (SQLException e) {
            throw dialect.convertException(e);
        }
    }

    <T> T nested(TransactionCallback<T> callback) {
        try {
            Savepoint savepoint = connection.setSavepoint();
            try {
                TransactionContext ctx = new TransactionContext(connection);
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
        } catch (SQLException e) {
            throw dialect.convertException(e);
        }
    }

    <T> T join(@NotNull TransactionCallback<T> callback) {
        try {
            return callback.execute(new TransactionContext(connection));
        } catch (SQLException e) {
            throw dialect.convertException(e);
        }
    }

    void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw dialect.convertException(e);
        }
    }
}
