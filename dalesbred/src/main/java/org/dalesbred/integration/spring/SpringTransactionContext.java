package org.dalesbred.integration.spring;

import org.dalesbred.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.TransactionStatus;

import java.sql.Connection;

import static java.util.Objects.requireNonNull;

final class SpringTransactionContext implements TransactionContext {

    private final @NotNull TransactionStatus status;

    private final @NotNull Connection connection;

    SpringTransactionContext(@NotNull TransactionStatus status, @NotNull Connection connection) {
        this.status = requireNonNull(status);
        this.connection = requireNonNull(connection);
    }

    @Override
    public @NotNull Connection getConnection() {
        return connection;
    }

    @Override
    public void setRollbackOnly() {
        status.setRollbackOnly();
    }

    @Override
    public boolean isRollbackOnly() {
        return status.isRollbackOnly();
    }
}
