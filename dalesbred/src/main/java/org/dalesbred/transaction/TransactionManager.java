package org.dalesbred.transaction;

import org.dalesbred.dialect.Dialect;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract the mechanism in which transactions are handled.
 */
public interface TransactionManager {

    /**
     * Executes given callback with given transaction settings.
     */
    <T> T withTransaction(@NotNull TransactionSettings settings, @NotNull TransactionCallback<T> callback, @NotNull Dialect dialect);

    /**
     * Executes given callback within current transaction.
     */
    <T> T withCurrentTransaction(@NotNull TransactionCallback<T> callback, @NotNull Dialect dialect);

    /**
     * Returns true if the code is executing inside transaction.
     */
    boolean hasActiveTransaction();
}
