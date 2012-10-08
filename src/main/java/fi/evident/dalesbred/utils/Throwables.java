package fi.evident.dalesbred.utils;

public final class Throwables {

    private Throwables() { }

    public static RuntimeException propagate(Throwable e) {
        if (e instanceof Error)
            throw (Error) e;
        else if (e instanceof RuntimeException)
            return (RuntimeException) e;
        else
            return new RuntimeException(e);
    }
}
