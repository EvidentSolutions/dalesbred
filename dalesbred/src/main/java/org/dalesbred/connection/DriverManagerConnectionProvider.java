package org.dalesbred.connection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *  {@link ConnectionProvider} that works opens connections from {@link DriverManager}
 *  and closes them when releasing.
 */
public final class DriverManagerConnectionProvider implements ConnectionProvider {

    private final @NotNull String url;

    private final @Nullable String user;

    private final @Nullable String password;

    public DriverManagerConnectionProvider(@NotNull String url, @Nullable String user, @Nullable String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public @NotNull Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public void releaseConnection(@NotNull Connection connection) throws SQLException {
        connection.close();
    }
}
