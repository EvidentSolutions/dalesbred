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

package org.dalesbred.tx;

import org.dalesbred.Isolation;
import org.dalesbred.Propagation;
import org.dalesbred.TransactionCallback;
import org.dalesbred.TransactionSettings;
import org.dalesbred.connection.ConnectionProvider;
import org.dalesbred.dialects.Dialect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;

import static org.dalesbred.internal.utils.Require.requireNonNull;

/**
 * Default transaction manager that handles all transactions by itself.
 */
public final class DefaultTransactionManager extends AbstractTransactionManager {

    /**
     * The current active transaction of this thread, or null
     */
    @NotNull
    private final ThreadLocal<DefaultTransaction> activeTransaction = new ThreadLocal<>();

    @NotNull
    private final ConnectionProvider connectionProvider;

    public DefaultTransactionManager(@NotNull ConnectionProvider connectionProvider) {
        this.connectionProvider = requireNonNull(connectionProvider);
    }

    @Override
    protected <T> T withNewTransaction(@NotNull TransactionCallback<T> callback,
                                       @NotNull Dialect dialect,
                                       @NotNull Isolation isolation,
                                       int retries) {
        Connection connection = openConnection(isolation, dialect);
        try {
            DefaultTransaction newTransaction = new DefaultTransaction(connection);
            activeTransaction.set(newTransaction);
            return newTransaction.execute(retries, callback, dialect);
        } finally {
            activeTransaction.set(null);
            releaseConnection(connection, dialect);
        }
    }

    @Override
    protected <T> T withSuspendedTransaction(@NotNull TransactionCallback<T> callback,
                                             @NotNull Isolation isolation,
                                             @NotNull Dialect dialect,
                                             int retries) {
        DefaultTransaction suspended = getActiveTransaction();
        try {
            activeTransaction.set(null);

            TransactionSettings settings = new TransactionSettings();
            settings.setRetries(retries);
            settings.setPropagation(Propagation.REQUIRED);
            settings.setIsolation(isolation);
            return withTransaction(settings, callback, dialect);
        } finally {
            activeTransaction.set(suspended);
        }
    }

    @Override
    @Nullable
    protected DefaultTransaction getActiveTransaction() {
        return activeTransaction.get();
    }

    @NotNull
    private Connection openConnection(@NotNull Isolation isolation, @NotNull Dialect dialect) {
        try {
            Connection connection = connectionProvider.getConnection();
            connection.setAutoCommit(false);
            if (isolation != Isolation.DEFAULT)
                connection.setTransactionIsolation(isolation.getJdbcLevel());

            return connection;
        } catch (SQLException e) {
            throw dialect.convertException(e);
        }
    }

    private void releaseConnection(@NotNull Connection connection, @NotNull Dialect dialect) {
        try {
            connectionProvider.releaseConnection(connection);
        } catch (SQLException e) {
            throw dialect.convertException(e);
        }
    }
}
