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
     * Returns the underlying JDBC connection.
     */
    public @NotNull Connection getConnection() {
        return connection;
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
