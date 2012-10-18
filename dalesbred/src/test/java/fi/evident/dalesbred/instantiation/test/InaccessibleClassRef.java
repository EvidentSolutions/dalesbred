package fi.evident.dalesbred.instantiation.test;

public final class InaccessibleClassRef {

    public static final Class<?> INACCESSIBLE_CLASS = InaccessibleClass.class;

    private InaccessibleClassRef() { }
}
