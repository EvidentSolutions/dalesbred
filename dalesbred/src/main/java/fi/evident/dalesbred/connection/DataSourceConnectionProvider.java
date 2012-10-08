package fi.evident.dalesbred.connection;

import fi.evident.dalesbred.JdbcException;

import javax.inject.Provider;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

/**
 * A provider for connection that delegates the requests to given {@link DataSource}.
 */
public final class DataSourceConnectionProvider implements Provider<Connection> {

    private final DataSource dataSource;

    public DataSourceConnectionProvider(DataSource dataSource) {
        this.dataSource = requireNonNull(dataSource);
    }

    @Override
    public Connection get() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }
}
