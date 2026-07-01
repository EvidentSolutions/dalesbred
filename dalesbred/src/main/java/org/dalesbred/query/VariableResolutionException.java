package org.dalesbred.query;

import org.dalesbred.DatabaseException;
import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when a variable could not be resolved.
 */
public class VariableResolutionException extends DatabaseException {

    public VariableResolutionException(@NotNull String message) {
        super(message);
    }

    public VariableResolutionException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }
}
