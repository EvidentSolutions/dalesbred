package org.dalesbred.transaction;

import org.dalesbred.DatabaseSQLException;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

/**
 * Exception thrown when transaction rolls back.
 *
 * @see TransactionSerializationException
 */
public class TransactionRollbackException extends DatabaseSQLException {
    public TransactionRollbackException(@NotNull SQLException cause) {
        super(cause);
    }
}
