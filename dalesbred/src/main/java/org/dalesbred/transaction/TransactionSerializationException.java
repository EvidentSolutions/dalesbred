package org.dalesbred.transaction;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

/**
 * Exception thrown when database can't achieve desired isolation level
 * due to concurrent updates.
 */
public class TransactionSerializationException extends TransactionRollbackException {
    public TransactionSerializationException(@NotNull SQLException cause) {
        super(cause);
    }
}
