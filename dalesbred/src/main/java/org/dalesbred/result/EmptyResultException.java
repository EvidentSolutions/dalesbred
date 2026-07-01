package org.dalesbred.result;

import org.dalesbred.Database;
import org.dalesbred.query.SqlQuery;

/**
 * Exception thrown when expecting a unique result from a call, but got no results.
 *
 * @see Database#findUnique(Class, SqlQuery)
 * @see NonUniqueResultException
 */
public class EmptyResultException extends NonUniqueResultException {

    public EmptyResultException() {
        super("Expected unique result, but got no rows");
    }
}
