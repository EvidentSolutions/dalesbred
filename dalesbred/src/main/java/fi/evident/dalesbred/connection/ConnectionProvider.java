package fi.evident.dalesbred.connection;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Abstracts the mechanism for opening and closing connections.
 */
public interface ConnectionProvider {

    @NotNull
    Connection getConnection() throws SQLException;
    void releaseConnection(@NotNull Connection connection) throws SQLException;
}
