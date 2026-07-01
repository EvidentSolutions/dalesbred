package org.dalesbred.transaction;

import org.dalesbred.connection.ConnectionProvider;
import org.dalesbred.dialect.Dialect;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Default transaction manager that handles all transactions by itself.
 */
public final class DefaultTransactionManager extends AbstractTransactionManager {

    /**
     * The current active transaction of this thread, or null
     */
    private final @NotNull ThreadLocal<DefaultTransaction> activeTransaction = new ThreadLocal<>();

    private final @NotNull ConnectionProvider connectionProvider;

    public DefaultTransactionManager(@NotNull ConnectionProvider connectionProvider) {
        this.connectionProvider = requireNonNull(connectionProvider);
    }

    @Override
    protected <T> T withNewTransaction(@NotNull TransactionCallback<T> callback,
                                       @NotNull Dialect dialect,
                                       @NotNull Isolation isolation) {
        Connection connection = openConnection(isolation, dialect);
        try {
            DefaultTransaction newTransaction = new DefaultTransaction(connection);
            activeTransaction.set(newTransaction);
            return newTransaction.execute(callback, dialect);
        } finally {
            activeTransaction.remove();
            releaseConnection(connection, dialect);
        }
    }

    @Override
    protected <T> T withSuspendedTransaction(@NotNull TransactionCallback<T> callback,
                                             @NotNull Isolation isolation,
                                             @NotNull Dialect dialect) {
        DefaultTransaction suspended = getActiveTransaction().orElse(null);
        try {
            activeTransaction.remove();

            TransactionSettings settings = new TransactionSettings();
            settings.setPropagation(Propagation.REQUIRED);
            settings.setIsolation(isolation);
            return withTransaction(settings, callback, dialect);
        } finally {
            activeTransaction.set(suspended);
        }
    }

    @Override
    protected @NotNull Optional<DefaultTransaction> getActiveTransaction() {
        return Optional.ofNullable(activeTransaction.get());
    }

    private @NotNull Connection openConnection(@NotNull Isolation isolation, @NotNull Dialect dialect) {
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
