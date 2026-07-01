package org.dalesbred.internal.utils;

import org.jetbrains.annotations.NotNull;

/**
 * Utilities for handling exceptions and other throwables.
 */
public final class Throwables {

    private Throwables() { }

    public static @NotNull RuntimeException propagate(@NotNull Throwable e) {
        if (e instanceof Error)
            throw (Error) e;
        else if (e instanceof RuntimeException)
            return (RuntimeException) e;
        else
            return new WrappedCheckedException(e);
    }

    public static @NotNull <T extends Exception> T propagate(@NotNull Throwable e, @NotNull Class<T> allowed) {
        if (allowed.isInstance(e))
            return allowed.cast(e);
        else
            throw propagate(e);
    }
}
