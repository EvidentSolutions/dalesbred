package fi.evident.dalesbred;

public class NonUniqueResultException extends DatabaseException {

    public NonUniqueResultException(int count) {
        super("Expected unique result but got " + count + " rows.");
    }
}
