package fi.evident.dalesbred;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

/**
 * Callback for executing a block of code within a transaction.
 *
 * @see Database#withTransaction(TransactionCallback)
 */
public interface TransactionCallback<T> {
    T execute(@NotNull TransactionContext tx) throws SQLException;
}
