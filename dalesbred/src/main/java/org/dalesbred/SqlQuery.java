/*
 * Copyright (c) 2015 Evident Solutions Oy
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

package org.dalesbred;

import org.dalesbred.query.NamedParameterQueries;
import org.dalesbred.query.VariableResolutionException;
import org.dalesbred.query.VariableResolver;
import org.dalesbred.query.VariableResolvers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.dalesbred.internal.utils.Require.requireNonNull;
import static org.dalesbred.query.VariableResolvers.resolverForBean;
import static org.dalesbred.query.VariableResolvers.resolverForMap;

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

    /**
     * Creates a new {@link SqlQuery} consisting of given SQL statement and arguments.
     * '?' characters act as placeholders for arguments in the query.
     */
    @NotNull
    public static SqlQuery query(@NotNull @SQL String sql, Object... args) {
        return new SqlQuery(sql, asList(args));
    }

    /**
     * @see #query(String, Object...)
     */
    @NotNull
    public static SqlQuery query(@NotNull @SQL String sql, @NotNull List<?> args) {
        return new SqlQuery(sql, args);
    }

    /**
     * Constructs a query with named arguments, using given map for resolving the values of arguments.
     *
     * @see #namedQuery(String, VariableResolver)
     * @see VariableResolvers#resolverForMap(Map)
     */
    @NotNull
    public static SqlQuery namedQuery(@NotNull @SQL String sql, @NotNull Map<String, ?> valueMap) {
        return namedQuery(sql, resolverForMap(valueMap));
    }

    /**
     * Constructs a query with named arguments, using the properties/fields of given bean for resolving arguments.
     *
     * @see #namedQuery(String, VariableResolver)
     * @see VariableResolvers#resolverForBean(Object)
     */
    @NotNull
    public static SqlQuery namedQuery(@NotNull @SQL String sql, @NotNull Object bean) {
        return namedQuery(sql, resolverForBean(bean));
    }

    /**
     * Creates a new {@link SqlQuery} consisting of given SQL statement and a provider for named arguments.
     * The argument names in SQL should be prefixed by a colon, e.g. ":argument".
     *
     * @throws SqlSyntaxException if SQL is malformed
     * @throws VariableResolutionException if variableResolver can't provide values for named parameters
     */
    @NotNull
    public static SqlQuery namedQuery(@NotNull @SQL String sql, @NotNull VariableResolver variableResolver) {
        return NamedParameterQueries.namedQuery(sql, variableResolver);
    }

    /**
     * Returns the SQL that is to be execute at the database. If the original query was a named query,
     * the variable placeholders will have been replaced by positional placeholders in this query.
     */
    @NotNull
    public String getSql() {
        return sql;
    }

    /**
     * Returns the list of arguments this query has.
     */
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
