package fi.evident.dalesbred;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

import static fi.evident.dalesbred.utils.Require.requireNonNull;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

public final class SqlQuery {
    final String sql;
    final List<Object> args;

    private SqlQuery(@NotNull @SQL String sql, Object... args) {
        this.sql = requireNonNull(sql);
        this.args = unmodifiableList(asList(args));
    }

    @NotNull
    public static SqlQuery query(@NotNull @SQL String sql, Object... args) {
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
}
