package fi.evident.dalesbred;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

/**
 * Provides transactions with access to the context.
 */
public final class TransactionContext {

    private final Connection connection;
    private boolean rollbackOnly = false;

    TransactionContext(@NotNull Connection connection) {
        this.connection = requireNonNull(connection);
    }

    /**
     * Returns the raw JDBC-connection for this transaction.
     */
    @NotNull
    public Connection getConnection() {
        return connection;
    }

    /**
     * Requests that this transaction will be rolled back.
     */
    public void setRollbackOnly() {
        rollbackOnly = true;
    }

    public boolean isRollbackOnly() {
        return rollbackOnly;
    }
}
