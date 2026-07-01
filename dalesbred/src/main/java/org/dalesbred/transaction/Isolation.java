package org.dalesbred.transaction;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

import static java.sql.Connection.*;

/**
 * Represents the database isolation levels.
 */
public enum Isolation {

    /** Use the default isolation level */
    DEFAULT(TRANSACTION_NONE),

    /** @see Connection#TRANSACTION_READ_UNCOMMITTED */
    READ_UNCOMMITTED(TRANSACTION_READ_UNCOMMITTED),

    /** @see Connection#TRANSACTION_READ_COMMITTED */
    READ_COMMITTED(TRANSACTION_READ_COMMITTED),

    /** @see Connection#TRANSACTION_REPEATABLE_READ */
    REPEATABLE_READ(TRANSACTION_REPEATABLE_READ),

    /** @see Connection#TRANSACTION_SERIALIZABLE */
    SERIALIZABLE(TRANSACTION_SERIALIZABLE);

    @JdbcIsolation
    private final int jdbcLevel;

    Isolation(@JdbcIsolation int jdbcLevel) {
        this.jdbcLevel = jdbcLevel;
    }

    /**
     * Returns the isolation value for given JDBC code.
     */
    public static @NotNull Isolation forJdbcCode(@JdbcIsolation int code) {
        for (Isolation isolation : values())
            if (isolation.jdbcLevel == code)
                return isolation;

        throw new IllegalArgumentException("invalid code: " + code);
    }

    /**
     * Returns the JDBC level for this isolation.
     */
    @JdbcIsolation
    public int getJdbcLevel() {
        return jdbcLevel;
    }

    @MagicConstant(intValues = { TRANSACTION_NONE, TRANSACTION_READ_COMMITTED, TRANSACTION_READ_UNCOMMITTED, TRANSACTION_REPEATABLE_READ, TRANSACTION_SERIALIZABLE })
    private @interface JdbcIsolation { }
}
