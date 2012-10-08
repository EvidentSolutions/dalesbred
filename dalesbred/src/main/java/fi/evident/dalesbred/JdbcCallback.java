package fi.evident.dalesbred;

import java.sql.SQLException;

/**
 * Callback for executing JDBC-operations in given context.
 */
public interface JdbcCallback<C,T> {
    T execute(C context) throws SQLException;
}
