package fi.evident.dalesbred;

import fi.evident.dalesbred.connection.DataSourceConnectionProvider;
import fi.evident.dalesbred.connection.DriverManagerConnectionProvider;
import fi.evident.dalesbred.dialects.Dialect;
import fi.evident.dalesbred.results.*;
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
import java.util.List;
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

    /** Transaction isolation level to use, or -1 to indicate default */
    private int transactionIsolation = -1;

    /** Do we want to create a new transaction if non-transactional calls are made */
    private boolean allowImplicitTransactions = true;

    @Nullable
    private Dialect dialect;

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
        if (transactionIsolation != -1)
            connection.setTransactionIsolation(transactionIsolation);

        return connection;
    }

    /**
     * Executes a query and processes the results with given {@link ResultSetProcessor}.
     * All other findXXX-methods are just convenience methods for this one.
     */
    public <T> T executeQuery(@NotNull final SqlQuery query, @NotNull final ResultSetProcessor<T> processor) {
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

    /**
     * Executes a query and processes each row of the result with given {@link RowMapper}
     * to produce a list of results.
     */
    @NotNull
    public <T> List<T> findAll(@NotNull SqlQuery query, @NotNull RowMapper<T> rowMapper) {
        return executeQuery(query, new ListWithRowMapperResultSetProcessor<T>(rowMapper));
    }

    /**
     * Executes a query and converts the results to instances of given class using default mechanisms.
     */
    @NotNull
    public <T> List<T> findAll(@NotNull SqlQuery query, @NotNull Class<T> cl) {
        return executeQuery(query, resultProcessorForClass(cl));
    }

    /**
     * Finds a unique result from database, using given {@link RowMapper} to convert the row.
     *
     * @throws NonUniqueResultException if there are no rows or multiple rows
     */
    @Nullable
    public <T> T findUnique(SqlQuery query, RowMapper<T> mapper) {
        return unique(findAll(query, mapper));
    }

    /**
     * Finds a unique result from database, converting the database row to given class using default mechanisms.
     *
     * @throws NonUniqueResultException if there are no rows or multiple rows
     */
    @Nullable
    public <T> T findUnique(SqlQuery query, Class<T> cl) {
        return unique(findAll(query, cl));
    }

    /**
     * Find a unique result from database, using given {@link RowMapper} to convert row. Returns null if
     * there are no results.
     *
     * @throws NonUniqueResultException if there are multiple result rows
     */
    @Nullable
    public <T> T findUniqueOrNull(SqlQuery query, RowMapper<T> rowMapper) {
        return uniqueOrNull(findAll(query, rowMapper));
    }

    /**
     * Finds a unique result from database, converting the database row to given class using default mechanisms.
     * Returns null if there are no results.
     *
     * @throws NonUniqueResultException if there are multiple result rows
     */
    @Nullable
    public <T> T findUniqueOrNull(SqlQuery query, Class<T> cl) {
        return uniqueOrNull(findAll(query, cl));
    }

    /**
     * A convenience method for retrieving a single non-null integer.
     */
    public int findUniqueInt(SqlQuery query) {
        Integer value = findUnique(query, Integer.class);
        if (value != null)
            return value;
        else
            throw new DatabaseException("database returned null instead of int");
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
    public int update(@NotNull @SQL final String sql, @NotNull final Object... args) {
        return update(query(sql, args));
    }

    /**
     * Executes an update against the database and processes the generated ids with given processor.
     */
    public <T> T updateAndReturnGeneratedKeys(@NotNull final SqlQuery query, @NotNull final ResultSetProcessor<T> resultSetProcessor) {
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

    /**
     * Executes an update against the database and returns the generated ids as given by row-mapper.
     */
    public <T> List<T> updateAndReturnGeneratedKeys(@NotNull final SqlQuery query, @NotNull final RowMapper<T> rowMapper) {
        return updateAndReturnGeneratedKeys(query, new ListWithRowMapperResultSetProcessor<T>(rowMapper));
    }

    /**
     * Executes an update against the database and returns the generated ids of given type.
     */
    public <T> List<T> updateAndReturnGeneratedKeys(@NotNull final SqlQuery query, @NotNull final Class<T> keyType) {
        return updateAndReturnGeneratedKeys(query, resultProcessorForClass(keyType));
    }

    private void logQuery(SqlQuery query) {
        if (log.isLoggable(Level.FINE))
            log.fine("executing query " + query);
    }

    private void bindArguments(PreparedStatement ps, List<Object> args) throws SQLException {
        Dialect provider = getDialect(ps.getConnection());
        int i = 1;

        for (Object arg : args)
            ps.setObject(i++, provider.valueToDatabase(arg));
    }

    @Nullable
    private static <T> T uniqueOrNull(@NotNull List<T> items) {
        switch (items.size()) {
            case 0:  return null;
            case 1:  return items.get(0);
            default: throw new NonUniqueResultException(items.size());
        }
    }

    @Nullable
    private static <T> T unique(@NotNull List<T> items) {
        if (items.size() == 1)
            return items.get(0);
        else
            throw new NonUniqueResultException(0);
    }

    @NotNull
    private static <T> ResultSetProcessor<List<T>> resultProcessorForClass(@NotNull Class<T> cl) {
        RowMapper<T> rowMapper = SingleColumnMappers.findRowMapperForType(cl);

        if (rowMapper != null)
            return new ListWithRowMapperResultSetProcessor<T>(rowMapper);
        else
            return ReflectionResultSetProcessor.forClass(cl);
    }

    @NotNull
    private Dialect getDialect(Connection connection) throws SQLException {
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

    public int getTransactionIsolation() {
        return transactionIsolation;
    }

    /**
     * Sets the transaction-isolation level to use.
     *
     * @see Connection#TRANSACTION_NONE
     * @see Connection#TRANSACTION_READ_UNCOMMITTED
     * @see Connection#TRANSACTION_READ_COMMITTED
     * @see Connection#TRANSACTION_REPEATABLE_READ
     * @see Connection#TRANSACTION_SERIALIZABLE
     */
    public void setTransactionIsolation(int level) {
        if (level != -1
                && level != Connection.TRANSACTION_NONE
                && level != Connection.TRANSACTION_READ_COMMITTED
                && level != Connection.TRANSACTION_READ_UNCOMMITTED
                && level != Connection.TRANSACTION_REPEATABLE_READ
                && level != Connection.TRANSACTION_SERIALIZABLE)
            throw new IllegalArgumentException("invalid isolation level: " + level);

        this.transactionIsolation = level;
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
