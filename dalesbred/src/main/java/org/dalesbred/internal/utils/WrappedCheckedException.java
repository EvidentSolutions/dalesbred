package org.dalesbred.internal.utils;

import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when when checked exception is thrown by some of the callbacks.
 */
public final class WrappedCheckedException extends RuntimeException {

    public WrappedCheckedException(@NotNull Throwable cause) {
        super(cause);
    }
}
