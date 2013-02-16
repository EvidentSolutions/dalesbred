/*
 * Copyright (c) 2012 Evident Solutions Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fi.evident.dalesbred;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static fi.evident.dalesbred.NamedParameterValueProviders.providerForBean;
import static fi.evident.dalesbred.NamedParameterValueProviders.providerForMap;
import static fi.evident.dalesbred.utils.Require.requireNonNull;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

/**
 * Represents an SQL query along all of its arguments.
 */
public final class SqlQuery implements Serializable {

    final String sql;
    final List<?> args;
    private static final long serialVersionUID = 1;

    private SqlQuery(@NotNull @SQL String sql, @NotNull List<?> args) {
        this.sql = requireNonNull(sql);
        this.args = unmodifiableList(args);
    }

    @NotNull
    public static SqlQuery query(@NotNull @SQL String sql, Object... args) {
        return new SqlQuery(sql, asList(args));
    }

    @NotNull
    public static SqlQuery query(@NotNull @SQL String sql, @NotNull List<?> args) {
        return new SqlQuery(sql, args);
    }

    @NotNull
    public static SqlQuery namedQuery(@NotNull @SQL String sql, @NotNull Map<String, ?> valueMap) {
        return namedQuery(sql, providerForMap(valueMap));
    }

    @NotNull
    public static SqlQuery namedQuery(@NotNull @SQL String sql, @NotNull Object valueBean) {
        return namedQuery(sql, providerForBean(valueBean));
    }

    @NotNull
    public static SqlQuery namedQuery(@NotNull @SQL String namedSql, @NotNull NamedParameterValueProvider valueProvider) {
        NamedParameterSql parsed = NamedParameterSqlParser.parseSqlStatement(namedSql);
        return query(parsed.getTraditionalSql(), parsed.toParameterValues(valueProvider));
    }

    @NotNull
    public String getSql() {
        return sql;
    }

    @NotNull
    public List<?> getArguments() {
        return args;
    }

    @NotNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(10 + sql.length() + 10 * args.size());

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

    /**
     * If the argument is a confidential value, returns it unwrapped, otherwise returns the value as it is.
     */
    @Nullable
    static Object unwrapConfidential(@Nullable Object arg) {
        if (arg instanceof ConfidentialValue)
            return ((ConfidentialValue) arg).value;
        else
            return arg;
    }

    /**
     * Wraps the value in a confidential wrapper which prevents it from ever being shown
     * in any logs or stack-traces, but it can still be used normally as a query-parameter.
     */
    public static Object confidential(Object value) {
        return new ConfidentialValue(value);
    }

    private static class ConfidentialValue {

        @Nullable
        private final Object value;

        private ConfidentialValue(@Nullable Object value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "****";
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;

            if (obj instanceof ConfidentialValue) {
                ConfidentialValue rhs = (ConfidentialValue) obj;
                return (value == null) ? rhs.value == null : value.equals(rhs.value);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return (value != null) ? value.hashCode() : 0;
        }
    }
}
