package org.dalesbred.transaction;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

/**
 * Provides transactions with access to the context.
 */
public interface TransactionContext {

    /**
     * Returns the raw JDBC-connection for this transaction.
     */
    @NotNull
    Connection getConnection();

    /**
     * Requests that this transaction will be rolled back.
     */
    void setRollbackOnly();

    /**
     * Returns whether this transaction has been marked for rollback.
     */
    boolean isRollbackOnly();
}
