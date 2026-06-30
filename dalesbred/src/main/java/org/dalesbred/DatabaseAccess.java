/*
 * Copyright (c) 2026 Evident Solutions Oy
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

import org.dalesbred.dialect.Dialect;
import org.dalesbred.internal.instantiation.InstantiatorProvider;
import org.dalesbred.internal.result.InstantiatorRowMapper;
import org.dalesbred.internal.result.MapResultSetProcessor;
import org.dalesbred.internal.result.ResultTableResultSetProcessor;
import org.dalesbred.query.FetchDirection;
import org.dalesbred.query.SqlQuery;
import org.dalesbred.result.*;
import org.dalesbred.transaction.TransactionCallback;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.Duration;
import java.util.*;

import static org.dalesbred.internal.utils.OptionalUtils.unwrapOptionalAsNull;

/**
 * Abstract base class for database access, providing all query and update operations
 * independently of how connections and transactions are managed.
 *
 * <p>The two concrete subclasses are:
 * <ul>
 *   <li>{@link Database} – the standard entry point; manages a connection pool or
 *       data source and handles transactions automatically</li>
 *   <li>{@link DatabaseConnection} – wraps a single caller-owned JDBC connection
 *       obtained from {@link DatabaseSource#openConnection()}</li>
 * </ul>
 */
public abstract class DatabaseAccess {

    /**
     * The dialect that the database uses
     */
    protected final @NotNull Dialect dialect;

    /**
     * Contains the instantiators and data-converters
     */
    protected final @NotNull InstantiatorProvider instantiatorRegistry;

    private final @NotNull Logger log = LoggerFactory.getLogger(DatabaseAccess.class);

    DatabaseAccess(@NotNull Dialect dialect, @NotNull InstantiatorProvider instantiatorRegistry) {
        this.dialect = Objects.requireNonNull(dialect);
        this.instantiatorRegistry = Objects.requireNonNull(instantiatorRegistry);
    }

    /**
     * default timeout set on all statements
     **/
    private @Nullable Duration defaultTimeout;

    /**
     * Executes a query and processes the results with given {@link ResultSetProcessor}.
     * All other findXXX-methods are just convenience methods for this one.
     */
    @SuppressWarnings("SqlSourceToSinkFlow")
    public <T> T executeQuery(@NotNull ResultSetProcessor<T> processor, @NotNull SqlQuery query) {
        return withCurrentTransaction(query, tx -> {
            logQuery(query);

            try (PreparedStatement ps = tx.getConnection().prepareStatement(query.getSql())) {
                prepareStatementFromQuery(ps, query);

                long startTime = System.nanoTime();
                try (ResultSet resultSet = ps.executeQuery()) {
                    logQueryExecution(query, System.nanoTime() - startTime);
                    return processor.process(resultSet);
                }
            }
        });
    }

    /**
     * Executes a query and processes the results with given {@link ResultSetProcessor}.
     *
     * @see #executeQuery(ResultSetProcessor, SqlQuery)
     */
    public <T> T executeQuery(@NotNull ResultSetProcessor<T> processor, @NotNull @Language("SQL") String sql, Object... args) {
        return executeQuery(processor, SqlQuery.query(sql, args));
    }

    /**
     * Executes a query and processes each row of the result with given {@link RowMapper}
     * to produce a list of results.
     */
    public @NotNull <T> List<T> findAll(@NotNull RowMapper<T> rowMapper, @NotNull SqlQuery query) {
        return executeQuery(rowMapper.list(), query);
    }

    /**
     * Executes a query and processes each row of the result with given {@link RowMapper}
     * to produce a list of results.
     */
    public @NotNull <T> List<T> findAll(@NotNull RowMapper<T> rowMapper, @NotNull @Language("SQL") String sql, Object... args) {
        return findAll(rowMapper, SqlQuery.query(sql, args));
    }

    /**
     * Executes a query and converts the results to instances of given class using default mechanisms.
     */
    public @NotNull <T> List<T> findAll(@NotNull Class<T> cl, @NotNull SqlQuery query) {
        return executeQuery(resultProcessorForClass(cl), query);
    }

    /**
     * Executes a query and converts the results to instances of given class using default mechanisms.
     */
    public @NotNull <T> List<T> findAll(@NotNull Class<T> cl, @NotNull @Language("SQL") String sql, Object... args) {
        return findAll(cl, SqlQuery.query(sql, args));
    }

    /**
     * Finds a unique result from database, using given {@link RowMapper} to convert the row.
     *
     * @throws NonUniqueResultException if there is more then one row
     * @throws EmptyResultException     if there are no rows
     */
    public <T> T findUnique(@NotNull RowMapper<T> mapper, @NotNull SqlQuery query) {
        return executeQuery(mapper.unique(), query);
    }

    /**
     * Finds a unique result from database, using given {@link RowMapper} to convert the row.
     *
     * @throws NonUniqueResultException if there is more then one row
     * @throws EmptyResultException     if there are no rows
     */
    public <T> T findUnique(@NotNull RowMapper<T> mapper, @NotNull @Language("SQL") String sql, Object... args) {
        return findUnique(mapper, SqlQuery.query(sql, args));
    }

    /**
     * Finds a unique result from database, converting the database row to given class using default mechanisms.
     *
     * @throws NonUniqueResultException if there is more then one row
     * @throws EmptyResultException     if there are no rows
     */
    public <T> T findUnique(@NotNull Class<T> cl, @NotNull SqlQuery query) {
        return executeQuery(rowMapperForClass(cl).unique(), query);
    }

    /**
     * Finds a unique result from database, converting the database row to given class using default mechanisms.
     *
     * @throws NonUniqueResultException if there is more then one row
     * @throws EmptyResultException     if there are no rows
     */
    public <T> T findUnique(@NotNull Class<T> cl, @NotNull @Language("SQL") String sql, Object... args) {
        return findUnique(cl, SqlQuery.query(sql, args));
    }

    /**
     * Find a unique result from database, using given {@link RowMapper} to convert row. Returns empty if
     * there are no results or if single null result is returned.
     *
     * @throws NonUniqueResultException if there are multiple result rows
     */
    public @NotNull <T> Optional<T> findOptional(@NotNull RowMapper<T> rowMapper, @NotNull SqlQuery query) {
        return executeQuery(rowMapper.optional(), query);
    }

    /**
     * Find a unique result from database, using given {@link RowMapper} to convert row. Returns empty if
     * there are no results or if single null result is returned.
     *
     * @throws NonUniqueResultException if there are multiple result rows
     */
    public @NotNull <T> Optional<T> findOptional(@NotNull RowMapper<T> rowMapper, @NotNull @Language("SQL") String sql, Object... args) {
        return findOptional(rowMapper, SqlQuery.query(sql, args));
    }

    /**
     * Finds a unique result from database, converting the database row to given class using default mechanisms.
     * Returns empty if there are no results or if single null result is returned.
     *
     * @throws NonUniqueResultException if there are multiple result rows
     */
    public @NotNull <T> Optional<T> findOptional(@NotNull Class<T> cl, @NotNull SqlQuery query) {
        return executeQuery(rowMapperForClass(cl).optional(), query);
    }

    /**
     * Finds a unique result from database, converting the database row to given class using default mechanisms.
     * Returns empty if there are no results or if single null result is returned.
     *
     * @throws NonUniqueResultException if there are multiple result rows
     */
    public @NotNull <T> Optional<T> findOptional(@NotNull Class<T> cl, @NotNull @Language("SQL") String sql, Object... args) {
        return findOptional(cl, SqlQuery.query(sql, args));
    }

    /**
     * Finds a unique result from database, converting the database row to int using default mechanisms.
     * Returns empty if there are no results or if single null result is returned.
     *
     * @throws NonUniqueResultException if there are multiple result rows
     */
    public @NotNull OptionalInt findOptionalInt(@NotNull @Language("SQL") String sql, Object... args) {
        return findOptionalInt(SqlQuery.query(sql, args));
    }

    /**
     * Finds a unique result from database, converting the database row to int using default mechanisms.
     * Returns empty if there are no results or if single null result is returned.
     *
     * @throws NonUniqueResultException if there are multiple result rows
     */
    public @NotNull OptionalInt findOptionalInt(@NotNull SqlQuery query) {
        Optional<Integer> value = findOptional(Integer.class, query);
        return value.map(OptionalInt::of).orElseGet(OptionalInt::empty);
    }

    /**
     * Finds a unique result from database, converting the database row to long using default mechanisms.
     * Returns empty if there are no results or if single null result is returned.
     *
     * @throws NonUniqueResultException if there are multiple result rows
     */
    public @NotNull OptionalLong findOptionalLong(@NotNull @Language("SQL") String sql, Object... args) {
        return findOptionalLong(SqlQuery.query(sql, args));
    }

    /**
     * Finds a unique result from database, converting the database row to long using default mechanisms.
     * Returns empty if there are no results or if single null result is returned.
     *
     * @throws NonUniqueResultException if there are multiple result rows
     */
    public @NotNull OptionalLong findOptionalLong(@NotNull SqlQuery query) {
        Optional<Long> value = findOptional(Long.class, query);
        return value.map(OptionalLong::of).orElseGet(OptionalLong::empty);
    }

    /**
     * Finds a unique result from database, converting the database row to double using default mechanisms.
     * Returns empty if there are no results or if single null result is returned.
     *
     * @throws NonUniqueResultException if there are multiple result rows
     */
    public @NotNull OptionalDouble findOptionalDouble(@NotNull @Language("SQL") String sql, Object... args) {
        return findOptionalDouble(SqlQuery.query(sql, args));
    }

    /**
     * Finds a unique result from database, converting the database row to double using default mechanisms.
     * Returns empty if there are no results or if single null result is returned.
     *
     * @throws NonUniqueResultException if there are multiple result rows
     */
    public @NotNull OptionalDouble findOptionalDouble(@NotNull SqlQuery query) {
        Optional<Double> value = findOptional(Double.class, query);
        return value.map(OptionalDouble::of).orElseGet(OptionalDouble::empty);
    }

    /**
     * Alias for {@code findOptional(rowMapper, query).orElse(null)}.
     */
    public @Nullable <T> T findUniqueOrNull(@NotNull RowMapper<T> rowMapper, @NotNull SqlQuery query) {
        return findOptional(rowMapper, query).orElse(null);
    }

    /**
     * Alias for {findUniqueOrNull(rowMapper, SqlQuery.query(sql, args))}.
     */
    public @Nullable <T> T findUniqueOrNull(@NotNull RowMapper<T> rowMapper, @NotNull @Language("SQL") String sql, Object... args) {
        return findUniqueOrNull(rowMapper, SqlQuery.query(sql, args));
    }

    /**
     * Alias for {@code findOptional(cl, query).orElse(null)}.
     */
    public @Nullable <T> T findUniqueOrNull(@NotNull Class<T> cl, @NotNull SqlQuery query) {
        return findOptional(cl, query).orElse(null);
    }

    /**
     * Alias for {@code findOptional(cl, sql, args).orElse(null)}.
     */
    public @Nullable <T> T findUniqueOrNull(@NotNull Class<T> cl, @NotNull @Language("SQL") String sql, Object... args) {
        return findOptional(cl, sql, args).orElse(null);
    }

    /**
     * A convenience method for retrieving a single non-null boolean.
     *
     * @throws NonUniqueResultException if there is more then one row
     * @throws EmptyResultException     if there are no rows
     */
    public boolean findUniqueBoolean(@NotNull SqlQuery query) {
        return executeQuery(rowMapperForClass(boolean.class).unique(), query);
    }

    /**
     * A convenience method for retrieving a single non-null boolean.
     *
     * @throws NonUniqueResultException if there is more then one row
     * @throws EmptyResultException     if there are no rows
     */
    public boolean findUniqueBoolean(@NotNull @Language("SQL") String sql, Object... args) {
        return findUniqueBoolean(SqlQuery.query(sql, args));
    }

    /**
     * A convenience method for retrieving a single non-null integer.
     *
     * @throws NonUniqueResultException if there is more then one row
     * @throws EmptyResultException     if there are no rows
     */
    public int findUniqueInt(@NotNull SqlQuery query) {
        return executeQuery(rowMapperForClass(int.class).unique(), query);
    }

    /**
     * A convenience method for retrieving a single non-null integer.
     *
     * @throws NonUniqueResultException if there is more than one row
     * @throws EmptyResultException     if there are no rows
     */
    public int findUniqueInt(@NotNull @Language("SQL") String sql, Object... args) {
        return findUniqueInt(SqlQuery.query(sql, args));
    }

    /**
     * A convenience method for retrieving a single non-null long.
     *
     * @throws NonUniqueResultException if there is more then one row
     * @throws EmptyResultException     if there are no rows
     */
    public long findUniqueLong(@NotNull SqlQuery query) {
        return executeQuery(rowMapperForClass(long.class).unique(), query);
    }

    /**
     * A convenience method for retrieving a single non-null long.
     *
     * @throws NonUniqueResultException if there is more then one row
     * @throws EmptyResultException     if there are no rows
     */
    public long findUniqueLong(@NotNull @Language("SQL") String sql, Object... args) {
        return findUniqueLong(SqlQuery.query(sql, args));
    }

    /**
     * Executes a query that returns at least two values and creates a map from the results,
     * using the first value as the key and rest of the values for instantiating {@code V}.
     *
     * <p>If the keys of the result are not distinct, the result contains the last binding of given key.
     */
    public @NotNull <K, V> Map<K, V> findMap(@NotNull Class<K> keyType,
                                             @NotNull Class<V> valueType,
                                             @NotNull SqlQuery query) {
        return executeQuery(new MapResultSetProcessor<>(keyType, valueType, instantiatorRegistry), query);
    }

    /**
     * Executes a query that returns at least two values and creates a map from the results,
     * using the first value as the key and rest of the values for instantiating {@code V}.
     *
     * <p>If the keys of the result are not distinct, the result contains the last binding of given key.
     */
    public @NotNull <K, V> Map<K, V> findMap(@NotNull Class<K> keyType,
                                             @NotNull Class<V> valueType,
                                             @NotNull @Language("SQL") String sql,
                                             Object... args) {
        return findMap(keyType, valueType, SqlQuery.query(sql, args));
    }

    /**
     * Executes a query and creates a {@link ResultTable} from the results.
     */
    public @NotNull ResultTable findTable(@NotNull SqlQuery query) {
        return executeQuery(new ResultTableResultSetProcessor(dialect), query);
    }

    /**
     * Executes a query and creates a {@link ResultTable} from the results.
     */
    public @NotNull ResultTable findTable(@NotNull @Language("SQL") String sql, Object... args) {
        return findTable(SqlQuery.query(sql, args));
    }

    /**
     * Executes an update against the database and returns the amount of affected rows.
     */
    @SuppressWarnings("SqlSourceToSinkFlow")
    public int update(@NotNull SqlQuery query) {
        return withCurrentTransaction(query, tx -> {
            logQuery(query);

            try (PreparedStatement ps = tx.getConnection().prepareStatement(query.getSql())) {
                prepareStatementFromQuery(ps, query);

                long startTime = System.nanoTime();
                int count = ps.executeUpdate();
                logQueryExecution(query, System.nanoTime() - startTime);
                return count;
            }
        });
    }

    /**
     * Executes an update against the database and returns the amount of affected rows.
     */
    public int update(@NotNull @Language("SQL") String sql, Object... args) {
        return update(SqlQuery.query(sql, args));
    }

    /**
     * Execute an update against the database and assert that a single row will be modified.
     *
     * @throws NonUniqueUpdateException if zero or more then one rows were updated
     */
    public void updateUnique(@NotNull SqlQuery query) {
        int modifiedRows = update(query);
        if (modifiedRows != 1)
            throw new NonUniqueUpdateException(modifiedRows);
    }

    /**
     * Execute an update against the database and assert that a single row will be modified.
     *
     * @throws NonUniqueUpdateException if zero or more then one rows were updated
     */
    public void updateUnique(@NotNull @Language("SQL") String sql, Object... args) {
        updateUnique(SqlQuery.query(sql, args));
    }

    /**
     * Executes an update against the database and return generated keys as extracted by generatedKeysProcessor.
     *
     * @param generatedKeysProcessor processor for handling the generated keys
     * @param columnNames            names of columns that contain the generated keys. Can be empty, in which case the
     *                               returned columns depend on the database
     * @param query                  to execute
     * @return Result of processing the results with {@code generatedKeysProcessor}.
     */
    public <T> T updateAndProcessGeneratedKeys(@NotNull ResultSetProcessor<T> generatedKeysProcessor, @NotNull List<String> columnNames, @NotNull SqlQuery query) {
        return withCurrentTransaction(query, tx -> {
            logQuery(query);

            try (PreparedStatement ps = prepareStatement(tx.getConnection(), query.getSql(), columnNames)) {
                prepareStatementFromQuery(ps, query);

                long startTime = System.nanoTime();
                ps.executeUpdate();
                logQueryExecution(query, System.nanoTime() - startTime);

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    return generatedKeysProcessor.process(rs);
                }
            }
        });
    }

    /**
     * @see #updateAndProcessGeneratedKeys(ResultSetProcessor, List, SqlQuery)
     */
    public <T> T updateAndProcessGeneratedKeys(@NotNull ResultSetProcessor<T> generatedKeysProcessor, @NotNull List<String> columnNames, @NotNull @Language("SQL") String sql, Object... args) {
        return updateAndProcessGeneratedKeys(generatedKeysProcessor, columnNames, SqlQuery.query(sql, args));
    }

    /**
     * Executes a batch update against the database, returning an array of modification
     * counts for each argument list.
     */
    @SuppressWarnings("SqlSourceToSinkFlow")
    public int[] updateBatch(@Language("SQL") @NotNull String sql, @NotNull List<? extends List<?>> argumentLists) {
        SqlQuery query = SqlQuery.query(sql, "<batch-update>");

        return withCurrentTransaction(query, tx -> {
            logQuery(query);

            try (PreparedStatement ps = tx.getConnection().prepareStatement(sql)) {
                bindQueryParameters(ps, query);
                for (List<?> arguments : argumentLists) {
                    bindArguments(ps, arguments);
                    ps.addBatch();
                }
                long startTime = System.nanoTime();
                int[] counts = ps.executeBatch();
                logQueryExecution(query, System.nanoTime() - startTime);
                return counts;
            }
        });
    }

    /**
     * Executes batch of updates against the database and return generated keys as extracted by generatedKeysProcessor.
     *
     * @param generatedKeysProcessor processor for handling the generated keys
     * @param columnNames            names of columns that contain the generated keys. Can be empty, in which case the
     *                               returned columns depend on the database
     * @param sql                    to execute
     * @param argumentLists          List of argument lists for items of batch
     * @return Result of processing the results with {@code generatedKeysProcessor}.
     */
    public <T> T updateBatchAndProcessGeneratedKeys(@NotNull ResultSetProcessor<T> generatedKeysProcessor,
                                                    @NotNull List<String> columnNames,
                                                    @NotNull @Language("SQL") String sql,
                                                    @NotNull List<? extends List<?>> argumentLists) {
        SqlQuery query = SqlQuery.query(sql, "<batch-update>");

        return withCurrentTransaction(query, tx -> {
            logQuery(query);

            try (PreparedStatement ps = prepareStatement(tx.getConnection(), sql, columnNames)) {
                bindQueryParameters(ps, query);
                for (List<?> arguments : argumentLists) {
                    bindArguments(ps, arguments);
                    ps.addBatch();
                }

                long startTime = System.nanoTime();
                ps.executeBatch();
                logQueryExecution(query, System.nanoTime() - startTime);

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    return generatedKeysProcessor.process(rs);
                }
            }
        });
    }

    private @NotNull <T> ResultSetProcessor<List<T>> resultProcessorForClass(@NotNull Class<T> cl) {
        return rowMapperForClass(cl).list();
    }

    private @NotNull <T> RowMapper<T> rowMapperForClass(@NotNull Class<T> cl) {
        return new InstantiatorRowMapper<>(cl, instantiatorRegistry);
    }

    /**
     * Returns the default query timeout, or {@code null} if none is set.
     */
    public @Nullable Duration getDefaultTimeout() {
        return defaultTimeout;
    }

    /**
     * Sets a default timeout applied to all queries unless overridden on the {@link SqlQuery} itself
     * or via JDBC connection parameters.
     *
     * @throws IllegalArgumentException if timeout is negative
     * @see Statement#setQueryTimeout(int)
     */
    public void setDefaultTimeout(@NotNull Duration timeout) {
        if (timeout.isNegative())
            throw new IllegalArgumentException("Negative timeout: " + timeout);

        this.defaultTimeout = timeout;
    }

    protected abstract <T> T withCurrentTransaction(@NotNull SqlQuery query, @NotNull TransactionCallback<T> callback);

    @SuppressWarnings("MagicConstant")
    protected void bindQueryParameters(@NotNull PreparedStatement ps, @NotNull SqlQuery query) throws SQLException {
        FetchDirection direction = query.getFetchDirection();
        if (direction != null)
            ps.setFetchDirection(direction.getJdbcCode());

        Integer fetchSize = query.getFetchSize();
        if (fetchSize != null)
            ps.setFetchSize(fetchSize);

        Duration timeout = query.getTimeout();
        if (timeout == null)
            timeout = defaultTimeout;

        if (timeout != null)
            ps.setQueryTimeout(Math.toIntExact(timeout.getSeconds()));
    }

    protected void bindArguments(@NotNull PreparedStatement ps, @NotNull Iterable<?> args) throws SQLException {
        int i = 1;
        for (Object arg : args)
            dialect.bindArgument(ps, i++, instantiatorRegistry.valueToDatabase(unwrapOptionalAsNull(arg)));
    }

    protected void prepareStatementFromQuery(@NotNull PreparedStatement ps, @NotNull SqlQuery query) throws SQLException {
        bindQueryParameters(ps, query);
        bindArguments(ps, query.getArguments());
    }

    private static @NotNull PreparedStatement prepareStatement(@NotNull Connection connection, @NotNull String sql, @NotNull List<String> columnNames) throws SQLException {
        if (columnNames.isEmpty())
            return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        else
            return connection.prepareStatement(sql, columnNames.toArray(EMPTY_STRING_ARRAY));
    }

    private void logQuery(@NotNull SqlQuery query) {
        log.debug("executing query {}", query);
    }

    private void logQueryExecution(@NotNull SqlQuery query, long nanos) {
        log.debug("executed query in {} ms: {}", nanos / 1_000_000, query);
    }

    private static final @NotNull String[] EMPTY_STRING_ARRAY = new String[0];
}
