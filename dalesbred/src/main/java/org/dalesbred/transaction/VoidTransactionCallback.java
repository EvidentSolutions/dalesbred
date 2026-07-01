package org.dalesbred.transaction;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

/**
 * Callback for executing a block of code within a transaction.
 *
 * @see TransactionCallback
 */
@FunctionalInterface
public interface VoidTransactionCallback {
    @SuppressWarnings("RedundantThrows")
    void execute(@NotNull TransactionContext tx) throws SQLException;
}
