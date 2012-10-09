package fi.evident.dalesbred;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

import static fi.evident.dalesbred.utils.Require.requireNonNull;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

/**
 * Represents an SQL query along all of its arguments.
 */
public final class SqlQuery {
    final String sql;
    final List<Object> args;

    private SqlQuery(@NotNull @SQL String sql, @NotNull List<Object> args) {
        this.sql = requireNonNull(sql);
        this.args = unmodifiableList(args);
    }

    @NotNull
    public static SqlQuery query(@NotNull @SQL String sql, Object... args) {
        return new SqlQuery(sql, asList(args));
    }

    @NotNull
    public static SqlQuery query(@NotNull @SQL String sql, @NotNull List<Object> args) {
        return new SqlQuery(sql, args);
    }

    @NotNull
    public String getSql() {
        return sql;
    }

    @NotNull
    public List<Object> getArguments() {
        return args;
    }

    @NotNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(sql);

        sb.append(" [");
        for (Iterator<?> it = args.iterator(); it.hasNext(); ) {
            sb.append(it.next());
            if (it.hasNext())
                sb.append(", ");
        }
        sb.append(']');

        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (obj instanceof SqlQuery) {
            SqlQuery rhs = (SqlQuery) obj;
            return sql.equals(rhs.sql) && args.equals(rhs.args);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return sql.hashCode() * 31 + args.hashCode();
    }
}
