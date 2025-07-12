/*
 * Copyright (c) 2017 Evident Solutions Oy
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

import org.dalesbred.Database;
import org.dalesbred.DatabaseException;
import org.dalesbred.dialect.Dialect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A {@link TransactionManager} that uses single underlying {@link Connection}.
 * Can be used to construct a {@link Database} with limited functionality
 * in situations where we need to use a specific connection (i.e. integration with third-party
 * frameworks that provide callbacks with just a connection).
 */
public final class SingleConnectionTransactionManager extends AbstractTransactionManager {

    private final @NotNull Connection connection;

    private @Nullable DefaultTransaction currentTransaction;

    /**
     * Constructs a transaction manager that uses given connection.
     *
     * @param connection to use for database access
     * @param insideForeignTransaction true iff transaction manager should assume that calls are inside
     *                                 a transactional context provided by third party framework
     */
    public SingleConnectionTransactionManager(@NotNull Connection connection,
                                              boolean insideForeignTransaction) {
        this.connection = requireNonNull(connection);
        this.currentTransaction = insideForeignTransaction ? new DefaultTransaction(connection) : null;
    }

    @Override
    protected @NotNull Optional<DefaultTransaction> getActiveTransaction() {
        return Optional.ofNullable(currentTransaction);
    }

    @Override
    protected <T> T withNewTransaction(@NotNull TransactionCallback<T> callback, @NotNull Dialect dialect, @NotNull Isolation isolation) {
        assert currentTransaction == null;

        try {
            connection.setAutoCommit(false);
            if (isolation != Isolation.DEFAULT)
                connection.setTransactionIsolation(isolation.getJdbcLevel());

            DefaultTransaction newTransaction = new DefaultTransaction(connection);
            currentTransaction = newTransaction;
            return newTransaction.execute(callback, dialect);
        } catch (SQLException e) {
            throw dialect.convertException(e);
        } finally {
            currentTransaction = null;
        }
    }

    @Override
    protected <T> T withSuspendedTransaction(@NotNull TransactionCallback<T> callback, @NotNull Isolation isolation, @NotNull Dialect dialect) {
        throw new DatabaseException("SingleConnectionTransactionManager does not support Suspended transactions.");
    }
}
