package org.dalesbred.result;

import org.dalesbred.Database;
import org.dalesbred.query.SqlQuery;
import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when expecting a unique result from a call, but more then one row
 * of results was returned by the database.
 *
 * @see Database#findUnique(Class, SqlQuery)
 * @see Database#findOptional(Class, SqlQuery)
 * @see Database#findUniqueOrNull(Class, SqlQuery)
 * @see EmptyResultException
 */
public class NonUniqueResultException extends UnexpectedResultException {

    public NonUniqueResultException() {
        super("Expected unique result but received more than one row");
    }

    protected NonUniqueResultException(@NotNull String message) {
        super(message);
    }
}
