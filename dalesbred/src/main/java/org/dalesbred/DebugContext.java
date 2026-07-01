package org.dalesbred;

import org.dalesbred.query.SqlQuery;
import org.jetbrains.annotations.Nullable;

final class DebugContext {

    private static final ThreadLocal<SqlQuery> currentQuery = new ThreadLocal<>();

    private DebugContext() { }

    static @Nullable SqlQuery getCurrentQuery() {
        return currentQuery.get();
    }

    static void setCurrentQuery(@Nullable SqlQuery query) {
        currentQuery.set(query);
    }
}
