package fi.evident.dalesbred.connection;

import fi.evident.dalesbred.DatabaseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Provider;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

/**
 * A simple provider for connections that simply fetches the connection from {@link DriverManager} without
 * providing any pooling.
 */
public final class DriverManagerConnectionProvider implements Provider<Connection> {

    private final String url;
    private String user;
    private String password;

    public DriverManagerConnectionProvider(@NotNull String url) {
        this(url, null, null);
    }

    public DriverManagerConnectionProvider(@NotNull String url, @Nullable String user, @Nullable String password) {
        this.url = requireNonNull(url);
        this.user = user;
        this.password = password;
    }

    @Override
    @NotNull
    public Connection get() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @NotNull
    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
