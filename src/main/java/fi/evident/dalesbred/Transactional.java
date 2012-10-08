package fi.evident.dalesbred;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;

public final class Transactional {
    private JdbcOperations jdbcOperations;

    @Inject
    public Transactional(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    public void in(final Transaction transaction) {
        jdbcOperations.withConnection(new JdbcCallback<Connection, Void>() {
            @Override
            public Void execute(Connection connection) throws SQLException {
                connection.setAutoCommit(false);
                connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

                try {
                    transaction.execute();
                    connection.commit();
                } catch (Exception e) {
                    System.out.println("rolled back transaction");
                    e.printStackTrace();
                    connection.rollback();
                    throw (RuntimeException) e;
                }

                return null; // doesn't matter, result is ignored for the method
            }
        });
    }

    public <ReturnType> ReturnType inReturning(final TransactionReturning<ReturnType> transaction) {
        return jdbcOperations.withConnection(new JdbcCallback<Connection, ReturnType>() {
            @Override
            public ReturnType execute(Connection connection) throws SQLException {
                connection.setAutoCommit(false);
                connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

                try {
                    ReturnType value = transaction.execute();
                    connection.commit();
                    return value;
                } catch (Exception e) {
                    System.out.println("rolled back transaction");
                    e.printStackTrace();
                    connection.rollback();
                    throw (RuntimeException) e;
                }
            }
        });
    }
}
