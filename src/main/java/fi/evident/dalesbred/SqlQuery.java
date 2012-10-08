package fi.evident.dalesbred;

import java.util.List;

import static fi.evident.dalesbred.utils.Require.requireNonNull;
import static java.util.Arrays.asList;

public final class SqlQuery {
    final String sql;
    final List<Object> args;

    private SqlQuery(@SQL String sql, Object... args) {
        this.sql = requireNonNull(sql);
        this.args = asList(args);
    }

    public static SqlQuery query(@SQL String sql, Object... args) {
        return new SqlQuery(sql, args);
    }
}
