/*
 * Copyright (c) 2017 Evident Solutions Oy
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

package org.dalesbred.query;

import org.dalesbred.annotation.SQL;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

/**
 * Represents an SQL query along all of its arguments.
 */
public final class SqlQuery implements Serializable {

    private final @NotNull String sql;

    private final @NotNull List<?> args;

    private @Nullable Integer fetchSize;

    private @Nullable FetchDirection fetchDirection;

    private @Nullable Duration timeout;

    private static final long serialVersionUID = 1;

    private SqlQuery(@NotNull @SQL String sql, @NotNull List<?> args) {
        this.sql = requireNonNull(sql);
        this.args = unmodifiableList(args);
    }

    /**
     * Creates a new {@link SqlQuery} consisting of given SQL statement and arguments.
     * '?' characters act as placeholders for arguments in the query.
     */
    public static @NotNull SqlQuery query(@NotNull @SQL String sql, Object... args) {
        return new SqlQuery(sql, asList(args));
    }

    /**
     * @see #query(String, Object...)
     */
    public static @NotNull SqlQuery query(@NotNull @SQL String sql, @NotNull List<?> args) {
        return new SqlQuery(sql, args);
    }

    /**
     * Constructs a query with named arguments, using given map for resolving the values of arguments.
     *
     * @see #namedQuery(String, VariableResolver)
     * @see VariableResolver#forMap(Map)
     */
    public static @NotNull SqlQuery namedQuery(@NotNull @SQL String sql, @NotNull Map<String, ?> valueMap) {
        return namedQuery(sql, VariableResolver.forMap(valueMap));
    }

    /**
     * Constructs a query with named arguments, using the properties/fields of given bean for resolving arguments.
     *
     * @see #namedQuery(String, VariableResolver)
     * @see VariableResolver#forBean(Object)
     */
    public static @NotNull SqlQuery namedQuery(@NotNull @SQL String sql, @NotNull Object bean) {
        return namedQuery(sql, VariableResolver.forBean(bean));
    }

    /**
     * Creates a new {@link SqlQuery} consisting of given SQL statement and a provider for named arguments.
     * The argument names in SQL should be prefixed by a colon, e.g. ":argument".
     *
     * @throws SqlSyntaxException if SQL is malformed
     * @throws VariableResolutionException if variableResolver can't provide values for named parameters
     */
    public static @NotNull SqlQuery namedQuery(@NotNull @SQL String sql, @NotNull VariableResolver variableResolver) {
        return NamedParameterSqlParser.parseSqlStatement(sql).toQuery(variableResolver);
    }

    /**
     * Returns the SQL that is to be execute at the database. If the original query was a named query,
     * the variable placeholders will have been replaced by positional placeholders in this query.
     */
    public @NotNull String getSql() {
        return sql;
    }

    /**
     * Returns the list of arguments this query has.
     */
    public @NotNull List<?> getArguments() {
        return args;
    }


    /**
     * Returns the fetch size of this query.
     */
    public @Nullable Integer getFetchSize() {
        return fetchSize;
    }

    /**
     * A non-null fetch size will be set as the fetch size for the statements executed from this query.
     * If the fetch size specified is zero, the JDBC driver ignores the value and makes its own best guess of the fetch size.
     *
     * @param size fetch size in rows or null
     * @throws IllegalArgumentException if size is < 0
     */
    public void setFetchSize(@Nullable Integer size) {
        if (size != null && size < 0)
            throw new IllegalArgumentException("Illegal fetch size " + size + ". Fetch size must be null or >= 0");
        this.fetchSize = size;
    }

    /**
     * Returns the fetch direction of this query.
     */
    public @Nullable FetchDirection getFetchDirection() {
        return fetchDirection;
    }

    /**
     * A non-null fetch direction will be set as the fetch direction for the statements executed from this query.
     *
     * @param direction fetch direction or null
     */
    public void setFetchDirection(@Nullable FetchDirection direction) {
        this.fetchDirection = direction;
    }

    /**
     * Returns the timeout of this query.
     */
    public @Nullable Duration getTimeout() {
        return timeout;
    }

    /**
     * A non-null timeout will be set as the timeout for the statements executed from this query.
     * If the timeout specified is zero, there is no limit for execution time
     * @see java.sql.Statement#setQueryTimeout(int)
     *
     * @param timeout {@link Duration} of timeout
     * @throws IllegalArgumentException if timeout is negative
     */
    public void setTimeout(@NotNull Duration timeout) {
        if (timeout.isNegative())
            throw new IllegalArgumentException("Illegal timeout " + timeout + ". Timeout must be non-negative");
        this.timeout = timeout;
    }



    @Override
    public @NotNull String toString() {
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
        return Objects.hash(sql, args);
    }
}
