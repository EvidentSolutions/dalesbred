package fi.evident.dalesbred.utils;

import org.jetbrains.annotations.NotNull;

public final class Throwables {

    private Throwables() { }

    @NotNull
    public static RuntimeException propagate(@NotNull Throwable e) {
        if (e instanceof Error)
            throw (Error) e;
        else if (e instanceof RuntimeException)
            return (RuntimeException) e;
        else
            return new RuntimeException(e);
    }

    @NotNull
    public static <T extends Exception> T propagate(@NotNull Throwable e, @NotNull Class<T> allowed) {
        if (allowed.isInstance(e))
            return allowed.cast(e);
        else
            throw propagate(e);
    }
}
