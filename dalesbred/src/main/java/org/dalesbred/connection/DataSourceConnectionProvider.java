package org.dalesbred.connection;

import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static java.util.Objects.requireNonNull;

/**
 * {@link ConnectionProvider} that works on top of {@link DataSource}.
 */
public final class DataSourceConnectionProvider implements ConnectionProvider {

    private final @NotNull DataSource dataSource;

    public DataSourceConnectionProvider(@NotNull DataSource dataSource) {
        this.dataSource = requireNonNull(dataSource);
    }

    @Override
    public @NotNull Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseConnection(@NotNull Connection connection) throws SQLException {
        connection.close();
    }
}
