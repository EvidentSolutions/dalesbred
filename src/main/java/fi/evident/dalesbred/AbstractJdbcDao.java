package fi.evident.dalesbred;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

/**
 * Super-simple abstraction to make JDBC a bit simpler.
 */
public abstract class AbstractJdbcDao {

    private JdbcOperations jdbcOperations;

    protected final SqlQuery query(@SQL String sql, Object... args) {
        return SqlQuery.query(sql, args);
    }

    protected final <T> T withConnection(JdbcCallback<Connection, T> callback) {
        return jdbcOperations.withConnection(callback);
    }

    protected final <T> List<T> list(SqlQuery query, final JdbcCallback<ResultSet, T> rowMapper) {
        return jdbcOperations.list(query, rowMapper);
    }

    protected final <T> List<T> list(SqlQuery query, Class<T> cl) {
        return jdbcOperations.list(query, cl);
    }

    protected final <T> T unique(SqlQuery query, Class<T> cl) {
        return jdbcOperations.unique(query, cl);
    }

    protected final <T> T query(final SqlQuery query, final JdbcCallback<ResultSet, T> callback) {
        return jdbcOperations.query(query, callback);
    }

    protected final <T> T insertAndReturnGeneratedId(Class<T> keyType, @SQL String sql, Object... args) {
        return jdbcOperations.insertAndReturnGeneratedId(keyType, sql, args);
    }

    protected final int update(@SQL final String sql, final Object... args) {
        return jdbcOperations.update(sql, args);
    }

    protected final int queryForInt(SqlQuery query) {
        return jdbcOperations.queryForInt(query);
    }

    protected final Integer queryForIntOrNull(SqlQuery query) {
        return jdbcOperations.queryForIntOrNull(query);
    }

    protected final String queryForString(SqlQuery query) {
        return jdbcOperations.queryForString(query);
    }

    @Inject
    public void setJdbcOperations(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }


}
