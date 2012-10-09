package fi.evident.dalesbred.connection;

import fi.evident.dalesbred.DatabaseException;
import org.jetbrains.annotations.NotNull;

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

    public DataSourceConnectionProvider(@NotNull DataSource dataSource) {
        this.dataSource = requireNonNull(dataSource);
    }

    @Override
    @NotNull
    public Connection get() {
        try {
            Connection connection = dataSource.getConnection();
            if (connection != null)
                return connection;
            else
                throw new DatabaseException("dataSource returned null connection");
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
