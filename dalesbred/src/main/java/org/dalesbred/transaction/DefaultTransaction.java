package org.dalesbred.transaction;

import org.dalesbred.dialect.Dialect;
import org.dalesbred.internal.utils.Throwables;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

import static java.util.Objects.requireNonNull;

final class DefaultTransaction {

    private final @NotNull Connection connection;

    private static final @NotNull Logger log = LoggerFactory.getLogger(DefaultTransaction.class);

    DefaultTransaction(@NotNull Connection connection) {
        this.connection = requireNonNull(connection);
    }

    <T> T execute(@NotNull TransactionCallback<T> callback, @NotNull Dialect dialect) {
        try {
            try {
                TransactionContext ctx = new DefaultTransactionContext(connection);
                T value = callback.execute(ctx);
                if (ctx.isRollbackOnly())
                    connection.rollback();
                else
                    connection.commit();
                return value;

            } catch (Exception e) {
                connection.rollback();
                log.warn("rolled back transaction because of exception: {}", e, e);
                throw Throwables.propagate(e, SQLException.class);
            }
        } catch (SQLException e) {
            throw dialect.convertException(e);
        }
    }

    <T> T nested(@NotNull TransactionCallback<T> callback, @NotNull Dialect dialect) {
        try {
            Savepoint savepoint = connection.setSavepoint();
            try {
                TransactionContext ctx = new DefaultTransactionContext(connection);
                T value = callback.execute(ctx);
                if (ctx.isRollbackOnly())
                    connection.rollback(savepoint);
                else
                    connection.releaseSavepoint(savepoint);
                return value;

            } catch (Exception e) {
                connection.rollback(savepoint);
                log.warn("rolled back nested transaction because of exception: {}", e, e);
                throw Throwables.propagate(e, SQLException.class);
            }
        } catch (SQLException e) {
            throw dialect.convertException(e);
        }
    }

    <T> T join(@NotNull TransactionCallback<T> callback, @NotNull Dialect dialect) {
        try {
            return callback.execute(new DefaultTransactionContext(connection));
        } catch (SQLException e) {
            throw dialect.convertException(e);
        }
    }
}
