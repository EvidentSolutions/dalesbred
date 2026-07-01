package org.dalesbred;

import org.dalesbred.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for all of Dalesbred's exceptions.
 */
public class DatabaseException extends RuntimeException {

    private final @Nullable SqlQuery query = DebugContext.getCurrentQuery();

    public DatabaseException(@NotNull String message) {
        super(message);
    }

    public DatabaseException(@NotNull Throwable cause) {
        super(cause);
    }
    
    public DatabaseException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }

    /**
     * If this exception was thrown during an execution of a query, returns the query. Otherwise
     * returns {@code null}.
     */
    public @Nullable SqlQuery getQuery() {
        return query;
    }

    @Override
    public @NotNull String toString() {
        String basicToString = super.toString();
        if (query != null)
            return basicToString + " (query: " + query + ')';
        else
            return basicToString;
    }
}
