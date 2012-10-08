package fi.evident.dalesbred;

import fi.evident.dalesbred.utils.StringUtils;
import org.postgresql.util.PGobject;

import javax.inject.Inject;
import javax.inject.Provider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static fi.evident.dalesbred.utils.Require.requireNonNull;
import static java.util.Arrays.asList;

public final class JdbcOperations {
    
    private final Provider<Connection> connectionProvider;
    private static final ThreadLocal<Connection> threadConnection = new ThreadLocal<Connection>();
    private final Logger log = Logger.getLogger(getClass().getName());
    
    @Inject
    public JdbcOperations(Provider<Connection> connectionProvider) {
        this.connectionProvider = requireNonNull(connectionProvider);
    }

    public <T> T withConnection(JdbcCallback<Connection, T> callback) {
        try {
            Connection connection = threadConnection.get();

            if (connection != null)
                return callback.execute(connection);

            connection = connectionProvider.get();
            if (connection == null)
                throw new JdbcException("connection-provider returned null connection");

            try {
                threadConnection.set(connection);
                return callback.execute(connection);
            } finally {
                threadConnection.set(null);
                connection.close();
            }
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
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
        return withConnection(new JdbcCallback<Connection, T>() {
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
        return withConnection(new JdbcCallback<Connection, Integer>() {
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
        return withConnection(new JdbcCallback<Connection, T>() {
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
        int i = 1;

        for (Object arg : args) {
            if(arg instanceof Collection) { // this is useful because of in clause needs dynamically sized collection
                Collection<?> collection = (Collection<?>)arg;

                for(Object o: collection) {
                    if(o instanceof JdbcTuple) {
                        JdbcTuple tuple = (JdbcTuple)o;

                        for (Object tupleChild : tuple.getObjectsInOrder()) {
                            ps.setObject(i++, valueToDatabase(tupleChild));
                        }
                    } else {
                        ps.setObject(i++, valueToDatabase(o));
                    }
                }
            }
            else
                ps.setObject(i++, valueToDatabase(arg));
        }
    }
    
    private static Object valueToDatabase(Object value) {
        if (value instanceof Enum<?>) {
            return createPGEnum((Enum<?>) value);
        }
        return value;
    }

    private static Object createPGEnum(Enum<?> value) {
        try {
            PGobject object = new PGobject();
            object.setType(databaseNameForEnumClass(value.getClass()));
            object.setValue(value.name());
            return object;
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    private static String databaseNameForEnumClass(Class<?> cl) {
        return StringUtils.upperCamelToLowerUnderscore(cl.getSimpleName());
    }
}
