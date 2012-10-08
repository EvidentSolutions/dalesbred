package fi.evident.dalesbred;

import fi.evident.dalesbred.connection.DataSourceConnectionProvider;
import fi.evident.dalesbred.connection.DriverManagerConnectionProvider;
import fi.evident.dalesbred.dialects.Dialect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static fi.evident.dalesbred.utils.Require.requireNonNull;
import static fi.evident.dalesbred.utils.Throwables.propagate;
import static java.util.Arrays.asList;

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
    private boolean allowImplicitTransactions = false;

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
            throw new JdbcException(e);
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
                throw new JdbcException(e);
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

    private Connection openConnection() throws SQLException {
        Connection connection = connectionProvider.get();
        if (connection == null)
            throw new JdbcException("connection-provider returned null connection");

        connection.setAutoCommit(false);
        if (transactionIsolation != -1)
            connection.setTransactionIsolation(transactionIsolation);

        return connection;
    }

    public <T> List<T> list(SqlQuery query, final JdbcCallback<ResultSet, T> rowMapper) {
        return find(query, new JdbcCallback<ResultSet, List<T>>() {
            @Override
            public List<T> execute(ResultSet rs) throws SQLException {
                List<T> result = new ArrayList<T>();

                while (rs.next())
                    result.add(rowMapper.execute(rs));

                return result;
            }
        });
    }

    public <T> List<T> list(SqlQuery query, Class<T> cl) {
        return find(query, ReflectionRowMapper.forClass(cl));
    }

    public <T> T unique(SqlQuery query, Class<T> cl) {
        List<T> items = list(query, cl);
        if (items.size() == 1)
            return items.get(0);
        else
            throw new JdbcException("Expected unique result but got " + items.size() + " rows.");
    }

    public <T> T find(final SqlQuery query, final JdbcCallback<ResultSet, T> callback) {
        return withCurrentTransaction(new ConnectionCallback<T>() {
            @Override
            public T execute(Connection connection) throws SQLException {
                if (log.isLoggable(Level.FINE))
                    log.fine("executing query " + query);

                PreparedStatement ps = connection.prepareStatement(query.sql);
                try {
                    bindArguments(ps, query.args);

                    ResultSet rs = ps.executeQuery();
                    try {
                        return callback.execute(rs);
                    } finally {
                        rs.close();
                    }
                } finally {
                    ps.close();
                }
            }
        });
    }

    public String queryForString(SqlQuery query) {
        return find(query, new JdbcCallback<ResultSet, String>() {
            @Override
            public String execute(ResultSet resultSet) throws SQLException {
                if (resultSet.next())
                    return resultSet.getString(1);
                else
                    throw new JdbcException("Expected at least one row.");
            }
        });
    }

    public String queryForStringOrNull(SqlQuery query) {
        return find(query, new JdbcCallback<ResultSet, String>() {
            @Override
            public String execute(ResultSet resultSet) throws SQLException {
                if (resultSet.next())
                    return resultSet.getString(1);
                else
                    return null;
            }
        });
    }

    public int queryForInt(SqlQuery query) {
        return find(query, new JdbcCallback<ResultSet, Integer>() {
            @Override
            public Integer execute(ResultSet resultSet) throws SQLException {
                if (resultSet.next())
                    return resultSet.getInt(1);
                else
                    throw new JdbcException("Expected at least one row.");
            }
        });
    }

    public Integer queryForIntOrNull(SqlQuery query) {
        return find(query, new JdbcCallback<ResultSet, Integer>() {
            @Override
            public Integer execute(ResultSet resultSet) throws SQLException {
                if (resultSet.next())
                    return resultSet.getInt(1);
                else
                    return null;
            }
        });
    }

    public int update(@SQL final String sql, final Object... args) {
        return withCurrentTransaction(new ConnectionCallback<Integer>() {
            @Override
            public Integer execute(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(sql);
                try {
                    bindArguments(ps, asList(args));
                    return ps.executeUpdate();
                } finally {
                    ps.close();
                }
            }
        });
    }

    public <T> T insertAndReturnGeneratedId(final Class<T> keyType, @SQL final String sql, final Object... args) {
        return withCurrentTransaction(new ConnectionCallback<T>() {
            @Override
            public T execute(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                try {
                    bindArguments(ps, asList(args));
                    ps.executeUpdate();

                    ResultSet keys = ps.getGeneratedKeys();
                    try {
                        if (keys.next())
                            return keyType.cast(keys.getObject(1));
                        else
                            throw new JdbcException("Expected a generated key, but did not receive one.");
                    } finally {
                        keys.close();
                    }
                } finally {
                    ps.close();
                }
            }
        });
    }

    private void bindArguments(PreparedStatement ps, List<Object> args) throws SQLException {
        Dialect provider = getDialect(ps.getConnection());
        int i = 1;

        for (Object arg : args)
            ps.setObject(i++, provider.valueToDatabase(arg));
    }

    @NotNull
    private Dialect getDialect(Connection connection) throws SQLException {
        if (dialect == null)
            dialect = Dialect.detect(connection);
        return dialect;
    }

    public void setDialect(@NotNull Dialect dialect) {
        this.dialect = requireNonNull(dialect);
    }

    public int getTransactionIsolation() {
        return transactionIsolation;
    }

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

    public void setAllowImplicitTransactions(boolean allowImplicitTransactions) {
        this.allowImplicitTransactions = allowImplicitTransactions;
    }
}
