package fi.evident.dalesbred;

public class NonUniqueResultException extends UnexpectedResultException {

    public NonUniqueResultException(int count) {
        super("Expected unique result but got " + count + " rows.");
    }
}
