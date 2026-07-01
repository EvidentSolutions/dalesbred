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
