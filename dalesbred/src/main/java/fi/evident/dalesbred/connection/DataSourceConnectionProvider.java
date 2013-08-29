package fi.evident.dalesbred.connection;

import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

/**
 * {@link ConnectionProvider} that works on top of {@link DataSource}.
 */
public final class DataSourceConnectionProvider implements ConnectionProvider {

    @NotNull
    private final DataSource dataSource;

    public DataSourceConnectionProvider(@NotNull DataSource dataSource) {
        this.dataSource = requireNonNull(dataSource);
    }

    @NotNull
    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseConnection(@NotNull Connection connection) throws SQLException {
        connection.close();
    }
}
