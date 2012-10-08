package fi.evident.dalesbred;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TransactionManager {

    private final JdbcOperations jdbcOperations;
    private int transactionIsolation = -1;
    private final Logger log = Logger.getLogger(getClass().getName());

    @Inject
    public TransactionManager(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    public void setTransactionIsolation(int transactionIsolation) {
        this.transactionIsolation = transactionIsolation;
    }

    public <ReturnType> ReturnType transactionally(final TransactionReturning<ReturnType> transaction) {
        return jdbcOperations.withConnection(new JdbcCallback<Connection, ReturnType>() {
            @Override
            public ReturnType execute(Connection connection) throws SQLException {
                connection.setAutoCommit(false);
                if (transactionIsolation != -1)
                    connection.setTransactionIsolation(transactionIsolation);

                try {
                    ReturnType value = transaction.execute();
                    connection.commit();
                    return value;
                } catch (RuntimeException e) {
                    log.log(Level.WARNING, "rolled back transaction: " + e, e);
                    connection.rollback();
                    throw e;
                }
            }
        });
    }
}
