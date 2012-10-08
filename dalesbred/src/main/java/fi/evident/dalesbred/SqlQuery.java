package fi.evident.dalesbred;

import java.util.Iterator;
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
