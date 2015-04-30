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

import org.dalesbred.annotation.SQL;
import org.dalesbred.connection.ConnectionProvider;
import org.dalesbred.connection.DataSourceConnectionProvider;
import org.dalesbred.connection.DriverManagerDataSourceProvider;
import org.dalesbred.dialect.Dialect;
import org.dalesbred.instantiation.DefaultInstantiatorRegistry;
import org.dalesbred.instantiation.InstantiatorRegistry;
import org.dalesbred.instantiation.TypeConversionRegistry;
import org.dalesbred.integration.proxy.TransactionalProxyFactory;
import org.dalesbred.query.SqlQuery;
import org.dalesbred.result.*;
import org.dalesbred.transaction.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.currentTimeMillis;
import static org.dalesbred.internal.utils.Require.requireNonNull;

/**
 * <p>
 * The main abstraction of the library: represents a configured connection to database and provides a way to
 * execute callbacks in transactions.
 * </p>
 * <p>
 * Usually you'll need only single instance of this in your application, unless you need to connect
 * several different databases or need different default settings for different use cases.
 * </p>
 */
public final class Database {

    /** Class responsible for transaction handling */
    @NotNull
    private final TransactionManager transactionManager;

    /** Logger in which we log actions */
    @NotNull
    private final Logger log = Logger.getLogger(getClass().getName());

    /** Default propagation for new transactions */
    private boolean allowImplicitTransactions = true;

    /** The dialect that the database uses */
    @NotNull
    private final Dialect dialect;

    /** Contains the instantiators and data-converters */
    @NotNull
    private final DefaultInstantiatorRegistry instantiatorRegistry;

    /**
     * Returns a new Database that uses given {@link DataSource} to retrieve connections.
     */
    @NotNull
    public static Database forDataSource(@NotNull DataSource dataSource) {
        return new Database(new DataSourceConnectionProvider(dataSource));
    }

    /**
     * Returns a new Database that uses {@link DataSource} with given JNDI-name.
     */
    @NotNull
    public static Database forJndiDataSource(@NotNull String jndiName) {
        try {
            InitialContext ctx = new InitialContext();
            try {
                DataSource dataSource = (DataSource) ctx.lookup(jndiName);
                if (dataSource != null)
                    return forDataSource(dataSource);
                else
                    throw new DatabaseException("Could not find DataSource '" + jndiName + '\'');
            } finally {
                ctx.close();
            }
        } catch (NamingException e) {
            throw new DatabaseException("Error when looking up DataSource '" + jndiName + "': " + e, e);
        }
    }

    /**
     * Returns a new Database that uses given connection options to open connection. The database
     * uses {@link DriverManagerDataSourceProvider} so it performs no connection pooling.
     *
     * @see DriverManagerDataSourceProvider
     */
    @NotNull
    public static Database forUrlAndCredentials(@NotNull String url, @Nullable String username, @Nullable String password) {
        return forDataSource(DriverManagerDataSourceProvider.createDataSource(url, username, password));
    }

    /**
     * Constructs a new Database that uses given {@link ConnectionProvider} and auto-detects the dialect to use.
     */
    public Database(@NotNull ConnectionProvider connectionProvider) {
        this(connectionProvider, Dialect.detect(connectionProvider));
    }

    /**
     * Constructs a new Database that uses given {@link ConnectionProvider} and {@link Dialect}.
     */
    public Database(@NotNull ConnectionProvider connectionProvider, @NotNull Dialect dialect) {
        this(new DefaultTransactionManager(connectionProvider), dialect);
    }

    /**
     * Constructs a new Database that uses given {@link TransactionManager} and auto-detects the dialect to use.
     */
    public Database(@NotNull TransactionManager transactionManager) {
        this(transactionManager, Dialect.detect(transactionManager));
    }

    /**
     * Constructs a new Database that uses given {@link TransactionManager} and {@link Dialect}.
     */
    public Database(@NotNull TransactionManager transactionManager, @NotNull Dialect dialect) {
        this.transactionManager = requireNonNull(transactionManager);
        this.dialect = requireNonNull(dialect);
        this.instantiatorRegistry = new DefaultInstantiatorRegistry(dialect);

        dialect.registerTypeConversions(instantiatorRegistry.getTypeConversionRegistry());
    }

    /**
     * Constructs a new Database that uses given {@link DataSource} and auto-detects the dialect to use.
     */
    public Database(@NotNull DataSource dataSource) {
        this(new DataSourceConnectionProvider(dataSource));
    }

    /**
     * Constructs a new Database that uses given {@link DataSource} and {@link Dialect}.
     */
    public Database(@NotNull DataSource dataSource, @NotNull Dialect dialect) {
        this(new DataSourceConnectionProvider(dataSource), dialect);
    }

    /**
     * Executes a block of code within a context of a transaction, using {@link Propagation#REQUIRED} propagation.
     */
    public <T> T withTransaction(@NotNull TransactionCallback<T> callback) {
        return withTransaction(Propagation.DEFAULT, Isolation.DEFAULT, callback);
    }

    /**
     * Executes a block of code with given propagation and configuration default isolation.
     */
    public <T> T withTransaction(@NotNull Propagation propagation, @NotNull TransactionCallback<T> callback) {
        return withTransaction(propagation, Isolation.DEFAULT, callback);
    }

    /**
     * Executes a block of code with given propagation and isolation.
     */
    public <T> T withTransaction(@NotNull Propagation propagation,
                                 @NotNull Isolation isolation,
                                 @NotNull TransactionCallback<T> callback) {

        TransactionSettings settings = new TransactionSettings();
        settings.setPropagation(propagation);
        settings.setIsolation(isolation);

        return withTransaction(settings, callback);
    }

    /**
     * Executes a block of code with given transaction settings.
     *
     * @see TransactionSettings
     */
    public <T> T withTransaction(@NotNull TransactionSettings settings,
                                 @NotNull TransactionCallback<T> callback) {

        return transactionManager.withTransaction(settings, callback, dialect);
    }

    /**
     * Executes a block of code within a context of a transaction, using {@link Propagation#REQUIRED} propagation.
     */
    public void withVoidTransaction(@NotNull VoidTransactionCallback callback) {
        withTransaction(convertCallback(callback));
    }

    /**
     * Executes a block of code with given propagation and configuration default isolation.
     */
    public void withVoidTransaction(@NotNull Propagation propagation, @NotNull VoidTransactionCallback callback) {
        withVoidTransaction(propagation, Isolation.DEFAULT, callback);
    }

    /**
     * Executes a block of code with given propagation and isolation.
     */
    public void withVoidTransaction(@NotNull Propagation propagation,
                                    @NotNull Isolation isolation,
                                    @NotNull VoidTransactionCallback callback) {
        withTransaction(propagation, isolation, convertCallback(callback));
    }

    /**
     * Executes a block of code with given transaction settings.
     *
     * @see TransactionSettings
     */
    public void withVoidTransaction(@NotNull TransactionSettings settings,
                                    @NotNull VoidTransactionCallback callback) {
        withTransaction(settings, convertCallback(callback));
    }

    @NotNull
    private static TransactionCallback<Void> convertCallback(@NotNull VoidTransactionCallback callback) {
        return tx -> {
            callback.execute(tx);
            //noinspection ReturnOfNull
            return null;
        };
    }

    /**
     * Returns true if and only if the current thread has an active transaction for this database.
     */
    public boolean hasActiveTransaction() {
        return transactionManager.hasActiveTransaction();
    }

    /**
     * Executes the block of code within context of current transaction. If there's no transaction in progress
     * throws {@link NoActiveTransactionException} unless implicit transaction are allowed: in this case, starts a new
     * transaction.
     *
     * @throws NoActiveTransactionException if there's no active transaction.
     * @see #setAllowImplicitTransactions(boolean)
     */
    private <T> T withCurrentTransaction(@NotNull SqlQuery query, @NotNull TransactionCallback<T> callback) {
        SqlQuery oldQuery = DebugContext.getCurrentQuery();
        try {
            DebugContext.setCurrentQuery(query);
            if (allowImplicitTransactions) {
                return withTransaction(callback);
            } else {
                return transactionManager.withCurrentTransaction(callback, dialect);
            }
        } finally {
            DebugContext.setCurrentQuery(oldQuery);
        }
    }

    /**
     * Executes a query and processes the results with given {@link ResultSetProcessor}.
     * All other findXXX-methods are just convenience methods for this one.
     */
    public <T> T executeQuery(@NotNull ResultSetProcessor<T> processor, @NotNull SqlQuery query) {
        return withCurrentTransaction(query, tx -> {
            logQuery(query);

            try (PreparedStatement ps = tx.getConnection().prepareStatement(query.getSql())) {
                bindArguments(ps, query.getArguments());

                long startTime = currentTimeMillis();
                try (ResultSet resultSet = ps.executeQuery()) {
                    logQueryExecution(query, currentTimeMillis() - startTime);
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
    public <T> T executeQuery(@NotNull ResultSetProcessor<T> processor, @NotNull @SQL String sql, Object... args) {
        return executeQuery(processor, SqlQuery.query(sql, args));
    }

    /**
     * Executes a query and processes each row of the result with given {@link RowMapper}
     * to produce a list of results.
     */
    @NotNull
    public <T> List<T> findAll(@NotNull RowMapper<T> rowMapper, @NotNull SqlQuery query) {
        return executeQuery(new ListWithRowMapperResultSetProcessor<>(rowMapper), query);
    }

    /**
     * Executes a query and processes each row of the result with given {@link RowMapper}
     * to produce a list of results.
     */
    @NotNull
    public <T> List<T> findAll(@NotNull RowMapper<T> rowMapper, @NotNull @SQL String sql, Object... args) {
        return findAll(rowMapper, SqlQuery.query(sql, args));
    }

    /**
     * Executes a query and converts the results to instances of given class using default mechanisms.
     */
    @NotNull
    public <T> List<T> findAll(@NotNull Class<T> cl, @NotNull SqlQuery query) {
        return executeQuery(resultProcessorForClass(cl), query);
    }

    /**
     * Executes a query and converts the results to instances of given class using default mechanisms.
     */
    @NotNull
    public <T> List<T> findAll(@NotNull Class<T> cl, @NotNull @SQL String sql, Object... args) {
        return findAll(cl, SqlQuery.query(sql, args));
    }

    /**
     * Finds a unique result from database, using given {@link RowMapper} to convert the row.
     *
     * @throws NonUniqueResultException if there are no rows or multiple rows
     */
    public <T> T findUnique(@NotNull RowMapper<T> mapper, @NotNull SqlQuery query) {
        return executeQuery(UniqueResultSetProcessor.unique(new ListWithRowMapperResultSetProcessor<>(mapper)), query);
    }

    /**
     * Finds a unique result from database, using given {@link RowMapper} to convert the row.
     *
     * @throws NonUniqueResultException if there are no rows or multiple rows
     */
    public <T> T findUnique(@NotNull RowMapper<T> mapper, @NotNull @SQL String sql, Object... args) {
        return findUnique(mapper, SqlQuery.query(sql, args));
    }

    /**
     * Finds a unique result from database, converting the database row to given class using default mechanisms.
     *
     * @throws NonUniqueResultException if there are no rows or multiple rows
     */
    public <T> T findUnique(@NotNull Class<T> cl, @NotNull SqlQuery query) {
        return executeQuery(UniqueResultSetProcessor.unique(resultProcessorForClass(cl)), query);
    }

    /**
     * Finds a unique result from database, converting the database row to given class using default mechanisms.
     *
     * @throws NonUniqueResultException if there are no rows or multiple rows
     */
    public <T> T findUnique(@NotNull Class<T> cl, @NotNull @SQL String sql, Object... args) {
        return findUnique(cl, SqlQuery.query(sql, args));
    }

    /**
     * Find a unique result from database, using given {@link RowMapper} to convert row. Returns null if
     * there are no results.
     *
     * @throws NonUniqueResultException if there are multiple result rows
     */
    @Nullable
    public <T> T findUniqueOrNull(@NotNull RowMapper<T> rowMapper, @NotNull SqlQuery query) {
        return executeQuery(UniqueResultSetProcessor.uniqueOrEmpty(new ListWithRowMapperResultSetProcessor<>(rowMapper)), query);
    }

    /**
     * Find a unique result from database, using given {@link RowMapper} to convert row. Returns null if
     * there are no results.
     *
     * @throws NonUniqueResultException if there are multiple result rows
     */
    @Nullable
    public <T> T findUniqueOrNull(@NotNull RowMapper<T> rowMapper, @NotNull @SQL String sql, Object... args) {
        return findUniqueOrNull(rowMapper, SqlQuery.query(sql, args));
    }

    /**
     * Finds a unique result from database, converting the database row to given class using default mechanisms.
     * Returns null if there are no results.
     *
     * @throws NonUniqueResultException if there are multiple result rows
     */
    @Nullable
    public <T> T findUniqueOrNull(@NotNull Class<T> cl, @NotNull SqlQuery query) {
        return executeQuery(UniqueResultSetProcessor.uniqueOrEmpty(resultProcessorForClass(cl)), query);
    }

    /**
     * Finds a unique result from database, converting the database row to given class using default mechanisms.
     * Returns null if there are no results.
     *
     * @throws NonUniqueResultException if there are multiple result rows
     */
    @Nullable
    public <T> T findUniqueOrNull(@NotNull Class<T> cl, @NotNull @SQL String sql, Object... args) {
        return findUniqueOrNull(cl, SqlQuery.query(sql, args));
    }

    /**
     * A convenience method for retrieving a single non-null integer.
     *
     * @throws NonUniqueResultException if there are no rows or multiple rows
     */
    public int findUniqueInt(@NotNull SqlQuery query) {
        return executeQuery(UniqueResultSetProcessor.unique(resultProcessorForClass(int.class)), query);
    }

    /**
     * A convenience method for retrieving a single non-null integer.
     *
     * @throws NonUniqueResultException if there are no rows or multiple rows
     */
    public int findUniqueInt(@NotNull @SQL String sql, Object... args) {
        return findUniqueInt(SqlQuery.query(sql, args));
    }

    /**
     * A convenience method for retrieving a single non-null long.
     */
    public long findUniqueLong(@NotNull SqlQuery query) {
        return executeQuery(UniqueResultSetProcessor.unique(resultProcessorForClass(long.class)), query);
    }

    /**
     * A convenience method for retrieving a single non-null integer.
     */
    public long findUniqueLong(@NotNull @SQL String sql, Object... args) {
        return findUniqueLong(SqlQuery.query(sql, args));
    }

    /**
     * Executes a query that returns two values and creates a map from the results,
     * using the first value as the key and second value as the value for that key.
     */
    @NotNull
    public <K,V> Map<K, V> findMap(@NotNull Class<K> keyType,
                                   @NotNull Class<V> valueType,
                                   @NotNull SqlQuery query) {
        return executeQuery(new MapResultSetProcessor<>(keyType, valueType, instantiatorRegistry), query);
    }

    /**
     * Executes a query that returns two values and creates a map from the results,
     * using the first value as the key and second value as the value for that key.
     */
    @NotNull
    public <K,V> Map<K, V> findMap(@NotNull Class<K> keyType,
                                   @NotNull Class<V> valueType,
                                   @NotNull @SQL String sql,
                                   Object... args) {
        return findMap(keyType, valueType, SqlQuery.query(sql, args));
    }

    /**
     * Executes a query and creates a {@link ResultTable} from the results.
     */
    @NotNull
    public ResultTable findTable(@NotNull SqlQuery query) {
        return executeQuery(new ResultTableResultSetProcessor(), query);
    }

    /**
     * Executes a query and creates a {@link ResultTable} from the results.
     */
    @NotNull
    public ResultTable findTable(@NotNull @SQL String sql, Object... args) {
        return findTable(SqlQuery.query(sql, args));
    }

    /**
     * Executes an update against the database and returns the amount of affected rows.
     */
    public int update(@NotNull SqlQuery query) {
        return withCurrentTransaction(query, tx -> {
            logQuery(query);

            try (PreparedStatement ps = tx.getConnection().prepareStatement(query.getSql())) {
                bindArguments(ps, query.getArguments());
                long startTime = currentTimeMillis();
                int count = ps.executeUpdate();
                logQueryExecution(query, currentTimeMillis() - startTime);
                return count;
            }
        });
    }

    /**
     * Executes an update against the database and returns the amount of affected rows.
     */
    public int update(@NotNull @SQL String sql, Object... args) {
        return update(SqlQuery.query(sql, args));
    }

    /**
     * Executes an update against the database and executes
     *
     * @param generatedKeysProcessor processor for handling the generated keys
     * @param columnNames names of columns that contain the generated keys. Can be empty, in which case the
     *                    returned columns depend on the database
     * @param query to execute
     * @return Result of processing the results with {@code generatedKeysProcessor}.
     */
    public <T> T updateAndProcessGeneratedKeys(@NotNull ResultSetProcessor<T> generatedKeysProcessor, @NotNull List<String> columnNames, @NotNull SqlQuery query) {
        return withCurrentTransaction(query, tx -> {
            logQuery(query);

            try (PreparedStatement ps = prepareStatement(tx.getConnection(), query.getSql(), columnNames)) {
                bindArguments(ps, query.getArguments());
                long startTime = currentTimeMillis();
                ps.executeUpdate();
                logQueryExecution(query, currentTimeMillis() - startTime);

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    return generatedKeysProcessor.process(rs);
                }
            }
        });
    }

    @NotNull
    private static PreparedStatement prepareStatement(@NotNull Connection connection, @NotNull String sql, @NotNull List<String> columnNames) throws SQLException {
        if (columnNames.isEmpty())
            return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        else
            return connection.prepareStatement(sql, columnNames.toArray(new String[columnNames.size()]));
    }

    /**
     * @see #updateAndProcessGeneratedKeys(ResultSetProcessor, List, SqlQuery)
     */
    public <T> T updateAndProcessGeneratedKeys(@NotNull ResultSetProcessor<T> generatedKeysProcessor, @NotNull List<String> columnNames, @NotNull @SQL String sql, Object... args) {
        return updateAndProcessGeneratedKeys(generatedKeysProcessor, columnNames, SqlQuery.query(sql, args));
    }

    /**
     * Executes a batch update against the database, returning an array of modification
     * counts for each argument list.
     */
    public int[] updateBatch(@SQL @NotNull String sql, @NotNull List<? extends  List<?>> argumentLists) {
        SqlQuery query = SqlQuery.query(sql, "<batch-update>");

        return withCurrentTransaction(query, tx -> {
            logQuery(query);

            try (PreparedStatement ps = tx.getConnection().prepareStatement(sql)) {
                for (List<?> arguments : argumentLists) {
                    bindArguments(ps, arguments);
                    ps.addBatch();
                }
                long startTime = currentTimeMillis();
                int[] counts = ps.executeBatch();
                logQueryExecution(query, currentTimeMillis() - startTime);
                return counts;
            }
        });
    }

    private void logQuery(@NotNull SqlQuery query) {
        if (log.isLoggable(Level.FINER))
            log.finer("executing query " + query);
    }

    private void logQueryExecution(@NotNull SqlQuery query, long millis) {
        if (log.isLoggable(Level.FINE))
            log.fine("executed query in " + millis + " ms: " + query);
    }

    private void bindArguments(@NotNull PreparedStatement ps, @NotNull Iterable<?> args) throws SQLException {
        int i = 1;

        for (Object arg : args)
            dialect.bindArgument(ps, i++, instantiatorRegistry.valueToDatabase(arg));
    }

    @NotNull
    private <T> ResultSetProcessor<List<T>> resultProcessorForClass(@NotNull Class<T> cl) {
        return new ReflectionResultSetProcessor<>(cl, instantiatorRegistry);
    }

    /**
     * Returns {@link TypeConversionRegistry} that can be used to register new type-conversions.
     */
    @NotNull
    public TypeConversionRegistry getTypeConversionRegistry() {
        return instantiatorRegistry.getTypeConversionRegistry();
    }

    /**
     * Returns {@link InstantiatorRegistry} that can be used to configure instantiation issues.
     */
    @NotNull
    public InstantiatorRegistry getInstantiatorRegistry() {
        return instantiatorRegistry;
    }

    /**
     * Returns a transactional proxy for given object.
     */
    @NotNull
    public <T> T createTransactionalProxyFor(@NotNull Class<T> iface, @NotNull T target) {
        return TransactionalProxyFactory.createTransactionalProxyFor(this, iface, target);
    }

    /**
     * Returns the used transaction isolation level.
     */
    @NotNull
    public Isolation getDefaultIsolation() {
        return transactionManager.getDefaultIsolation();
    }

    /**
     * Sets the transaction isolation level to use.
     */
    public void setDefaultIsolation(@NotNull Isolation isolation) {
        transactionManager.setDefaultIsolation(isolation);
    }

    /**
     * Returns the default transaction propagation to use.
     */
    @NotNull
    public Propagation getDefaultPropagation() {
        return transactionManager.getDefaultPropagation();
    }

    /**
     * Returns the default transaction propagation to use.
     */
    public void setDefaultPropagation(@NotNull Propagation propagation) {
        transactionManager.setDefaultPropagation(propagation);
    }

    /**
     * If flag is set to true (by default it's false) queries without active transaction will
     * not throw exception but will start a fresh transaction.
     */
    public boolean isAllowImplicitTransactions() {
        return allowImplicitTransactions;
    }

    /**
     * If flag is set to true (by default it's false) queries without active transaction will
     * not throw exception but will start a fresh transaction.
     */
    public void setAllowImplicitTransactions(boolean allowImplicitTransactions) {
        this.allowImplicitTransactions = allowImplicitTransactions;
    }

    /**
     * Sets the way enumerations are persisted.
     */
    public void setEnumMode(@NotNull EnumMode enumMode) {
        dialect.setEnumMode(enumMode);
    }

    /**
     * Gets the way enumerations are persisted.
     */
    @NotNull
    public EnumMode getEnumMode() {
        return dialect.getEnumMode();
    }

    /**
     * Returns the dialect that the database is using.
     */
    @NotNull
    public Dialect getDialect() {
        return dialect;
    }

    /**
     * Returns a string containing useful debug information about the state of this object.
     */
    @Override
    @NotNull
    public String toString() {
        return "Database [dialect=" + dialect + ", allowImplicitTransactions=" + allowImplicitTransactions + ", defaultIsolation=" + transactionManager.getDefaultIsolation() + ", defaultPropagation=" + transactionManager.getDefaultPropagation() + ']';
    }
}
