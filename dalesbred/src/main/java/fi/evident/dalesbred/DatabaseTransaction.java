package fi.evident.dalesbred;

import org.jetbrains.annotations.NotNull;

import javax.inject.Provider;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.logging.Level;
import java.util.logging.Logger;

import static fi.evident.dalesbred.utils.Throwables.propagate;

/**
 * Represents the active database-transaction.
 */
final class DatabaseTransaction {

    private final Connection connection;
    private static final Logger log = Logger.getLogger(DatabaseTransaction.class.getName());

    DatabaseTransaction(@NotNull Provider<Connection> connectionProvider, Isolation isolation) {
        this.connection = connectionProvider.get();
        if (connection == null)
            throw new DatabaseException("connection-provider returned null connection");

        try {
            connection.setAutoCommit(false);

            if (isolation != null)
                connection.setTransactionIsolation(isolation.level);

        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    <T> T execute(@NotNull final TransactionCallback<T> callback) {
        try {
            try {
                T value = callback.execute(connection);
                connection.commit();
                return value;

            } catch (Exception e) {
                connection.rollback();
                log.log(Level.WARNING, "rolled back transaction because of exception: " + e, e);
                throw propagate(e, SQLException.class);
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    <T> T nested(TransactionCallback<T> callback) {
        try {
            Savepoint savepoint = connection.setSavepoint();
            try {
                T value = callback.execute(connection);
                connection.releaseSavepoint(savepoint);
                return value;

            } catch (Exception e) {
                connection.rollback(savepoint);
                log.log(Level.WARNING, "rolled back nested transaction because of exception: " + e, e);
                throw propagate(e, SQLException.class);
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    <T> T join(@NotNull TransactionCallback<T> callback) {
        try {
            return callback.execute(connection);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
