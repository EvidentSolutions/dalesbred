package org.dalesbred.transaction;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

import static java.util.Objects.requireNonNull;

final class DefaultTransactionContext implements TransactionContext {

    private final @NotNull Connection connection;
    private boolean rollbackOnly = false;

    DefaultTransactionContext(@NotNull Connection connection) {
        this.connection = requireNonNull(connection);
    }

    /**
     * Returns the raw JDBC-connection for this transaction.
     */
    @Override
    public @NotNull Connection getConnection() {
        return connection;
    }

    /**
     * Requests that this transaction will be rolled back.
     */
    @Override
    public void setRollbackOnly() {
        rollbackOnly = true;
    }

    @Override
    public boolean isRollbackOnly() {
        return rollbackOnly;
    }
}
