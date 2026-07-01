package org.dalesbred.query;

import java.sql.ResultSet;

/**
 * Represents fetch direction that can be given as a hint to {@link SqlQuery#setFetchDirection(FetchDirection)}.
 *
 * @see java.sql.PreparedStatement#setFetchDirection(int)
 */
public enum FetchDirection {
    FORWARD(ResultSet.FETCH_FORWARD),
    REVERSE(ResultSet.FETCH_REVERSE),
    UNKNOWN(ResultSet.FETCH_UNKNOWN);

    private final int jdbcCode;

    FetchDirection(int jdbcCode) {
        this.jdbcCode = jdbcCode;
    }

    public int getJdbcCode() {
        return jdbcCode;
    }
}
