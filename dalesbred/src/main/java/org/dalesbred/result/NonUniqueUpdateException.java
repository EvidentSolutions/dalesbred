package org.dalesbred.result;

import org.dalesbred.Database;
import org.dalesbred.query.SqlQuery;

/**
 * Exception thrown when expecting update to modify a single row, but zero or multiple rows were modified.
 *
 * @see Database#updateUnique(SqlQuery)
 */
public class NonUniqueUpdateException extends UnexpectedResultException {

    public NonUniqueUpdateException(int count) {
        super("Expected single row to be updated, but database updated " + count + " rows");
    }
}
