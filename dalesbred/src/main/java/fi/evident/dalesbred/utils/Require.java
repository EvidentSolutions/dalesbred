package fi.evident.dalesbred.utils;

public final class Require {

    private Require() { }

    /**
     * Returns value if it is not {@code null}, otherwise throws {@link NullPointerException}.
     */
    public static <T> T requireNonNull(T value) {
        if (value == null) throw new NullPointerException();

        return value;
    }
}
