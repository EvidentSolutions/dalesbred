package fi.evident.dalesbred;

import java.sql.Connection;

/**
 * Represents the database isolation levels.
 */
public enum Isolation {

    READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
    READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
    REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
    SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

    final int level;

    Isolation(int level) {
        this.level = level;
    }
}
