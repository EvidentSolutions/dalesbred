/*
 * Copyright (c) 2026 Evident Solutions Oy
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

import org.dalesbred.dialect.Dialect;
import org.dalesbred.internal.instantiation.InstantiatorProvider;
import org.dalesbred.query.SqlQuery;
import org.dalesbred.transaction.TransactionCallback;
import org.dalesbred.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A database connection with an explicit lifetime managed by the caller.
 * Wraps a single JDBC {@link Connection} opened from a {@link DatabaseSource}.
 *
 * <p>Instances are obtained from {@link DatabaseSource#openConnection()}, not constructed
 * directly. Use try-with-resources to ensure the connection is always closed:
 *
 * <pre>{@code
 * try (DatabaseConnection conn = source.openConnection()) {
 *     conn.update("insert into orders ...");
 *     conn.update("insert into order_items ...");
 * }
 * }</pre>
 *
 * @see DatabaseSource#openConnection()
 */
public final class DatabaseConnection extends DatabaseAccess implements AutoCloseable {

    @NotNull
    private final Connection connection;

    private boolean rollbackOnly;

    DatabaseConnection(@NotNull Connection connection,
                       @NotNull Dialect dialect,
                       @NotNull InstantiatorProvider instantiatorRegistry) {
        super(dialect, instantiatorRegistry);
        this.connection = Objects.requireNonNull(connection);
    }

    @Override
    protected <T> T withCurrentTransaction(@NotNull SqlQuery query, @NotNull TransactionCallback<T> callback) {
        SqlQuery oldQuery = DebugContext.getCurrentQuery();
        DebugContext.setCurrentQuery(query);
        try {
            return callback.execute(new TransactionContext() {
                @Override
                public @NotNull Connection getConnection() {
                    return connection;
                }

                @Override
                public void setRollbackOnly() {
                    rollbackOnly = true;
                }

                @Override
                public boolean isRollbackOnly() {
                    return rollbackOnly;
                }
            });
        } catch (SQLException e) {
            throw new DatabaseSQLException(e);
        } finally {
            DebugContext.setCurrentQuery(oldQuery);
        }
    }


    /**
     * Marks the current transaction as rollback-only, indicating that the transaction
     * should be rolled back rather than committed when it is finalized.
     */
    public void setRollbackOnly() {
        rollbackOnly = true;
    }

    /**
     * Commits the pending transaction and closes the underlying JDBC connection.
     *
     * @throws DatabaseException if the commit, rollback, or close raises a SQL error
     */
    @Override
    public void close() {
        try {
            try {
                if (rollbackOnly)
                    this.connection.rollback();
                else
                    this.connection.commit();
                this.connection.close();
            } catch (SQLException e) {
                try {
                    this.connection.close();
                } catch (SQLException e2) {
                    e.addSuppressed(e2);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new DatabaseSQLException(e);
        }
    }
}
