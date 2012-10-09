package fi.evident.dalesbred;

/**
 * Exception thrown when result from database is unexpected.
 */
public class UnexpectedResultException extends DatabaseException {
    public UnexpectedResultException(String message) {
        super(message);
    }
}
