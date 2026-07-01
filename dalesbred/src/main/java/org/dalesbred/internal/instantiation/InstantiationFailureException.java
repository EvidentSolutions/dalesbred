package org.dalesbred.internal.instantiation;

import org.dalesbred.DatabaseException;
import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when there is a problem with instantiation or conversion.
 */
public class InstantiationFailureException extends DatabaseException {

    public InstantiationFailureException(@NotNull String message) {
        super(message);
    }

    public InstantiationFailureException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }
}
