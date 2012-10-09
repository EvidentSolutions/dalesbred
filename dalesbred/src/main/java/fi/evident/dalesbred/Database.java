package fi.evident.dalesbred;

import fi.evident.dalesbred.connection.DataSourceConnectionProvider;
import fi.evident.dalesbred.connection.DriverManagerConnectionProvider;
import fi.evident.dalesbred.dialects.Dialect;
import fi.evident.dalesbred.instantiation.Coercion;
import fi.evident.dalesbred.instantiation.InstantiatorRegistry;
import fi.evident.dalesbred.instantiation.NamedTypeList;
import fi.evident.dalesbred.results.ListWithRowMapperResultSetProcessor;
import fi.evident.dalesbred.results.ReflectionResultSetProcessor;
import fi.evident.dalesbred.results.ResultSetProcessor;
import fi.evident.dalesbred.results.RowMapper;
import fi.evident.dalesbred.utils.ResultSetUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static fi.evident.dalesbred.SqlQuery.query;
import static fi.evident.dalesbred.utils.Require.requireNonNull;
import static fi.evident.dalesbred.utils.Throwables.propagate;

/**
 * The main abstraction of the library: represents a connection to database and provides a way to
 * execute callbacks in transactions.
 */
public final class Database {

    /** Provides us with connections whenever we need one */
    private final Provider<Connection> connectionProvider;

    /** A reference to the thread-local connection to make sure we don't open a new one if we are in context of transaction */
    private final ThreadLocal<Connection> activeTransactionConnection = new ThreadLocal<Connection>();

    /** Logger in which we log actions */
    private final Logger log = Logger.getLogger(getClass().getName());

    /** Transaction isolation level to use, or null to indicate default */
    @Nullable
    private Isolation transactionIsolation = null;

    /** Do we want to create a new transaction if non-transactional calls are made */
    private boolean allowImplicitTransactions = true;

    @Nullable
    private Dialect dialect;

    /** Instantiators */
    @Nullable
    private InstantiatorRegistry instantiatorRegistry;

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
            DataSource dataSource = (DataSource) ctx.lookup(jndiName);
            if (dataSource != null)
                return forDataSource(dataSource);
            else
                throw new DatabaseException("Could not find DataSource '" + jndiName + "'");
        } catch (NamingException e) {
            throw new DatabaseException("Error when looking up DataSource '" + jndiName + "': " + e, e);
        }
    }

    /**
     * Returns a new Database that uses given connection options to open connection. The database
     * uses {@link DriverManagerConnectionProvider} so it performs no connection pooling.
     *
     * @see DriverManagerConnectionProvider
     */
    @NotNull
    public static Database forUrlAndCredentials(@NotNull String url, String username, String password) {
        return new Database(new DriverManagerConnectionProvider(url, username, password));
    }

    /**
     * Constructs a new Database that uses given connection-provider.
     */
    @Inject
    public Database(@NotNull Provider<Connection> connectionProvider) {
        this.connectionProvider = requireNonNull(connectionProvider);
    }

    /**
     * Executes a block of code within a context of a transaction. If there is already an active
     * transaction in the thread, joins the transaction, otherwise starts a new transaction. If
     * an exception reaches the outermost transaction, the transaction will be rolled back.
     */
    public <T> T withTransaction(@NotNull ConnectionCallback<T> callback) {
        try {
            Connection connection = activeTransactionConnection.get();

            if (connection != null)
                return callback.execute(connection);

            connection = openConnection();

            try {
                activeTransactionConnection.set(connection);
                return executeTransactionally(connection, callback);
            } finally {
                activeTransactionConnection.set(null);
                connection.close();
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Executes the block of code within context of current transaction. If there's no transaction in progress
     * throws {@link IllegalStateException} unless implicit transaction are allowed: in this case, starts a new
     * transaction.
     *
     * @throws IllegalStateException if there's no active transaction.
     * @see #setAllowImplicitTransactions(boolean)
     */
    private <T> T withCurrentTransaction(@NotNull ConnectionCallback<T> callback) {
        Connection connection = activeTransactionConnection.get();
        if (connection != null) {
            try {
                return callback.execute(connection);
            } catch (SQLException e) {
                throw new DatabaseException(e);
            }
        } else {
            if (allowImplicitTransactions)
                return withTransaction(callback);
            else
                throw new IllegalStateException("No active transaction. Database accesses should be bracketed with Database.withTransaction(...)");
        }
    }

    private <T> T executeTransactionally(@NotNull Connection connection, @NotNull final ConnectionCallback<T> callback) throws SQLException {
        try {
            T value = callback.execute(connection);
            connection.commit();
            return value;

        } catch (Exception e) {
            connection.rollback();
            log.log(Level.WARNING, "rolled back transaction because of exception: " + e, e);
            throw propagate(e, SQLException.class);
        }
    }

    @NotNull
    private Connection openConnection() throws SQLException {
        Connection connection = connectionProvider.get();
        if (connection == null)
            throw new DatabaseException("connection-provider returned null connection");

        connection.setAutoCommit(false);

        Isolation isolation = transactionIsolation;
        if (isolation != null)
            connection.setTransactionIsolation(isolation.level);

        return connection;
    }

    /**
     * Executes a query and processes the results with given {@link ResultSetProcessor}.
     * All other findXXX-methods are just convenience methods for this one.
     */
    public <T> T executeQuery(@NotNull final ResultSetProcessor<T> processor, @NotNull final SqlQuery query) {
        return withCurrentTransaction(new ConnectionCallback<T>() {
            @Override
            public T execute(Connection connection) throws SQLException {
                logQuery(query);

                PreparedStatement ps = connection.prepareStatement(query.sql);
                try {
                    bindArguments(ps, query.args);

                    return processResults(ps.executeQuery(), processor);
                } finally {
                    ps.close();
                }
            }
        });
    }

    public <T> T executeQuery(@NotNull ResultSetProcessor<T> processor, @NotNull @SQL String sql, Object... args) {
        return executeQuery(processor, query(sql, args));
    }

    /**
     * Executes a query and processes each row of the result with given {@link RowMapper}
     * to produce a list of results.
     */
    @NotNull
    public <T> List<T> findAll(@NotNull RowMapper<T> rowMapper, @NotNull SqlQuery query) {
        return executeQuery(new ListWithRowMapperResultSetProcessor<T>(rowMapper), query);
    }

    @NotNull
    public <T> List<T> findAll(@NotNull RowMapper<T> rowMapper, @NotNull @SQL String sql, Object... args) {
        return findAll(rowMapper, query(sql, args));
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
        return findAll(cl, query(sql, args));
    }

    /**
     * Finds a unique result from database, using given {@link RowMapper} to convert the row.
     *
     * @throws NonUniqueResultException if there are no rows or multiple rows
     */
    public <T> T findUnique(@NotNull RowMapper<T> mapper, @NotNull SqlQuery query) {
        return unique(findAll(mapper, query));
    }

    public <T> T findUnique(@NotNull RowMapper<T> mapper, @NotNull @SQL String sql, Object... args) {
        return findUnique(mapper, query(sql, args));
    }

    /**
     * Finds a unique result from database, converting the database row to given class using default mechanisms.
     *
     * @throws NonUniqueResultException if there are no rows or multiple rows
     */
    public <T> T findUnique(@NotNull Class<T> cl, @NotNull SqlQuery query) {
        return unique(findAll(cl, query));
    }

    public <T> T findUnique(@NotNull Class<T> cl, @NotNull @SQL String sql, Object... args) {
        return findUnique(cl, query(sql, args));
    }

    /**
     * Find a unique result from database, using given {@link RowMapper} to convert row. Returns null if
     * there are no results.
     *
     * @throws NonUniqueResultException if there are multiple result rows
     */
    @Nullable
    public <T> T findUniqueOrNull(@NotNull RowMapper<T> rowMapper, @NotNull SqlQuery query) {
        return uniqueOrNull(findAll(rowMapper, query));
    }

    @Nullable
    public <T> T findUniqueOrNull(@NotNull RowMapper<T> rowMapper, @NotNull @SQL String sql, Object... args) {
        return findUniqueOrNull(rowMapper, query(sql, args));
    }

    /**
     * Finds a unique result from database, converting the database row to given class using default mechanisms.
     * Returns null if there are no results.
     *
     * @throws NonUniqueResultException if there are multiple result rows
     */
    @Nullable
    public <T> T findUniqueOrNull(@NotNull Class<T> cl, @NotNull SqlQuery query) {
        return uniqueOrNull(findAll(cl, query));
    }

    public <T> T findUniqueOrNull(@NotNull Class<T> cl, @NotNull @SQL String sql, Object... args) {
        return findUniqueOrNull(cl, query(sql, args));
    }

    /**
     * A convenience method for retrieving a single non-null integer.
     */
    public int findUniqueInt(@NotNull SqlQuery query) {
        Integer value = findUnique(Integer.class, query);
        if (value != null)
            return value;
        else
            throw new DatabaseException("database returned null instead of int");
    }

    /**
     * A convenience method for retrieving a single non-null integer.
     */
    public int findUniqueInt(@NotNull @SQL String sql, Object... args) {
        return findUniqueInt(query(sql, args));
    }

    @NotNull
    public <K,V> Map<K, V> findMap(@NotNull final Class<K> keyType,
                                   @NotNull final Class<V> valueType,
                                   @NotNull SqlQuery query) {

        ResultSetProcessor<Map<K,V>> processor = new ResultSetProcessor<Map<K, V>>() {
            @Override
            public Map<K,V> process(@NotNull ResultSet resultSet) throws SQLException {
                final Map<K,V> result = new LinkedHashMap<K,V>();
                InstantiatorRegistry instantiatorRegistry = getInstantiatorRegistry();

                NamedTypeList types = ResultSetUtils.getTypes(resultSet.getMetaData());
                if (types.size() != 2)
                    throw new DatabaseException("expected result-set with 2 columns, but got " + types.size() + " columns");

                @SuppressWarnings("unchecked")
                Class<Object> keySource = (Class) types.getType(0);
                @SuppressWarnings("unchecked")
                Class<Object> valueSource = (Class) types.getType(1);

                Coercion<Object,K> keyCoercion = instantiatorRegistry.getCoercionFromDbValue(keySource, keyType);
                Coercion<Object,V> valueCoercion = instantiatorRegistry.getCoercionFromDbValue(valueSource, valueType);

                while (resultSet.next()) {
                    Object key = resultSet.getObject(1);
                    Object value = resultSet.getObject(2);
                    result.put(keyCoercion.coerce(key), valueCoercion.coerce(value));
                }
                return result;
            }
        };
        return executeQuery(processor, query);
    }

    @NotNull
    public <K,V> Map<K, V> findMap(@NotNull Class<K> keyType,
                                   @NotNull Class<V> valueType,
                                   @NotNull @SQL String query,
                                   Object... args) {
        return findMap(keyType, valueType, query(query, args));
    }

    /**
     * Executes an update against the database and returns the amount of affected rows.
     */
    public int update(@NotNull final SqlQuery query) {
        return withCurrentTransaction(new ConnectionCallback<Integer>() {
            @Override
            public Integer execute(Connection connection) throws SQLException {
                logQuery(query);

                PreparedStatement ps = connection.prepareStatement(query.sql);
                try {
                    bindArguments(ps, query.args);
                    return ps.executeUpdate();
                } finally {
                    ps.close();
                }
            }
        });
    }

    /**
     * Executes an update against the database and returns the amount of affected rows.
     */
    public int update(@NotNull @SQL String sql, Object... args) {
        return update(query(sql, args));
    }

    /**
     * Executes an update against the database and processes the generated ids with given processor.
     */
    public <T> T updateAndReturnGeneratedKeys(@NotNull final ResultSetProcessor<T> resultSetProcessor, @NotNull final SqlQuery query) {
        return withCurrentTransaction(new ConnectionCallback<T>() {
            @Override
            public T execute(Connection connection) throws SQLException {
                logQuery(query);

                PreparedStatement ps = connection.prepareStatement(query.sql, PreparedStatement.RETURN_GENERATED_KEYS);
                try {
                    bindArguments(ps, query.args);
                    ps.executeUpdate();

                    return processResults(ps.getGeneratedKeys(), resultSetProcessor);

                } finally {
                    ps.close();
                }
            }
        });
    }

    public <T> T updateAndReturnGeneratedKeys(@NotNull final ResultSetProcessor<T> resultSetProcessor, @NotNull @SQL String sql, Object... args) {
        return updateAndReturnGeneratedKeys(resultSetProcessor, query(sql, args));
    }

    /**
     * Executes an update against the database and returns the generated ids as given by row-mapper.
     */
    @NotNull
    public <T> List<T> updateAndReturnGeneratedKeys(@NotNull final RowMapper<T> rowMapper, @NotNull final SqlQuery query) {
        return updateAndReturnGeneratedKeys(new ListWithRowMapperResultSetProcessor<T>(rowMapper), query);
    }

    @NotNull
    public <T> List<T> updateAndReturnGeneratedKeys(@NotNull final RowMapper<T> rowMapper, @NotNull @SQL String sql, Object... args) {
        return updateAndReturnGeneratedKeys(rowMapper, query(sql, args));
    }

    /**
     * Executes an update against the database and returns the generated ids of given type.
     */
    @NotNull
    public <T> List<T> updateAndReturnGeneratedKeys(@NotNull Class<T> keyType, @NotNull SqlQuery query) {
        return updateAndReturnGeneratedKeys(resultProcessorForClass(keyType), query);
    }

    @NotNull
    public <T> List<T> updateAndReturnGeneratedKeys(@NotNull Class<T> keyType, @NotNull @SQL String sql, Object... args) {
        return updateAndReturnGeneratedKeys(keyType, query(sql, args));
    }

    @NotNull
    public <T> T updateAndReturnGeneratedKey(@NotNull Class<T> keyType, @NotNull SqlQuery query) {
        T key = unique(updateAndReturnGeneratedKeys(keyType, query));
        if (key != null)
            return key;
        else
            throw new DatabaseException("null key was generated");
    }

    @NotNull
    public <T> T updateAndReturnGeneratedKey(@NotNull Class<T> keyType, @NotNull @SQL String sql, Object... args) {
        return updateAndReturnGeneratedKey(keyType, query(sql, args));
    }

    private void logQuery(@NotNull SqlQuery query) {
        if (log.isLoggable(Level.FINE))
            log.fine("executing query " + query);
    }

    private void bindArguments(@NotNull PreparedStatement ps, @NotNull List<Object> args) throws SQLException {
        InstantiatorRegistry instantiatorRegistry = getInstantiatorRegistry();
        int i = 1;

        for (Object arg : args)
            ps.setObject(i++, instantiatorRegistry.valueToDatabase(arg));
    }

    @Nullable
    private static <T> T uniqueOrNull(@NotNull List<T> items) {
        switch (items.size()) {
            case 0:  return null;
            case 1:  return items.get(0);
            default: throw new NonUniqueResultException(items.size());
        }
    }

    private static <T> T unique(@NotNull List<T> items) {
        if (items.size() == 1)
            return items.get(0);
        else
            throw new NonUniqueResultException(items.size());
    }

    @NotNull
    private <T> ResultSetProcessor<List<T>> resultProcessorForClass(@NotNull Class<T> cl) {
        return new ReflectionResultSetProcessor<T>(cl, getInstantiatorRegistry());
    }

    @NotNull
    private InstantiatorRegistry getInstantiatorRegistry() {
        if (instantiatorRegistry == null) {
            instantiatorRegistry = withTransaction(new ConnectionCallback<InstantiatorRegistry>() {
                @Override
                public InstantiatorRegistry execute(@NotNull Connection connection) throws SQLException {
                    return new InstantiatorRegistry(getDialect(connection));
                }
            });
        }

        return instantiatorRegistry;
    }

    @NotNull
    private Dialect getDialect(@NotNull Connection connection) throws SQLException {
        if (dialect == null)
            dialect = Dialect.detect(connection);
        return dialect;
    }

    private static <T> T processResults(@NotNull ResultSet resultSet, @NotNull ResultSetProcessor<T> processor) throws SQLException {
        try {
            return processor.process(resultSet);
        } finally {
            resultSet.close();
        }
    }

    public void setDialect(@NotNull Dialect dialect) {
        this.dialect = requireNonNull(dialect);
    }

    /**
     * Returns the used transaction isolation level, or null for default leve.
     */
    @Nullable
    public Isolation getTransactionIsolation() {
        return transactionIsolation;
    }

    /**
     * Sets the transaction isolation level to use, or null for default level
     */
    public void setTransactionIsolation(@Nullable Isolation isolation) {
        this.transactionIsolation = isolation;
    }

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
}
