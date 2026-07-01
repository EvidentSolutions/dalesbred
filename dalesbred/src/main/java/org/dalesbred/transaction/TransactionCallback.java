package org.dalesbred.transaction;

import org.dalesbred.Database;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

/**
 * Callback for executing a block of code within a transaction.
 *
 * @see Database#withTransaction(TransactionCallback)
 * @see VoidTransactionCallback
 */
@SuppressWarnings({"UnusedParameters", "SameReturnValue"})
@FunctionalInterface
public interface TransactionCallback<T> {
    T execute(@NotNull TransactionContext tx) throws SQLException;

    /**
     * Converts {@code VoidTransactionCallback} to {@code TransactionCallback<Void>}
     */
    static @NotNull TransactionCallback<Void> fromVoidCallback(@NotNull VoidTransactionCallback callback) {
        return tx -> {
            callback.execute(tx);
            //noinspection ReturnOfNull
            return null;
        };
    }
}
