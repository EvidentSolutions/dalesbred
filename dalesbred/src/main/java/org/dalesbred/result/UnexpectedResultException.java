package org.dalesbred.result;

import org.dalesbred.DatabaseException;
import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when result from database is unexpected.
 */
public class UnexpectedResultException extends DatabaseException {
    public UnexpectedResultException(@NotNull String message) {
        super(message);
    }
}
