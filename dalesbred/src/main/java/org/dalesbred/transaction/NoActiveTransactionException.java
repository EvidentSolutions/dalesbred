package org.dalesbred.transaction;

import org.dalesbred.DatabaseException;
import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when performing an operation that requires a transaction without having an active transaction.
 */
public class NoActiveTransactionException extends DatabaseException {
    public NoActiveTransactionException(@NotNull String message) {
        super(message);
    }
}
