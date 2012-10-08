package fi.evident.dalesbred.results;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Mappers for single column results.
 */
public final class SingleColumnMappers {

    private SingleColumnMappers() { }

    private static final RowMapper<Boolean> BOOLEAN = new RowMapper<Boolean>() {
        @Override
        public Boolean mapRow(ResultSet resultSet) throws SQLException {
            boolean value = resultSet.getBoolean(1);
            return resultSet.wasNull() ? null : value;
        }
    };

    private static final RowMapper<Short> SHORT = new RowMapper<Short>() {
        @Override
        public Short mapRow(ResultSet resultSet) throws SQLException {
            short value = resultSet.getShort(1);
            return resultSet.wasNull() ? null : value;
        }
    };

    private static final RowMapper<Integer> INTEGER = new RowMapper<Integer>() {
        @Override
        public Integer mapRow(ResultSet resultSet) throws SQLException {
            int value = resultSet.getInt(1);
            return resultSet.wasNull() ? null : value;
        }
    };

    private static final RowMapper<Long> LONG = new RowMapper<Long>() {
        @Override
        public Long mapRow(ResultSet resultSet) throws SQLException {
            long value = resultSet.getLong(1);
            return resultSet.wasNull() ? null : value;
        }
    };

    private static final RowMapper<String> STRING = new RowMapper<String>() {
        @Override
        public String mapRow(ResultSet resultSet) throws SQLException {
            return resultSet.getString(1);
        }
    };

    private static final RowMapper<Object> OBJECT = new RowMapper<Object>() {
        @Override
        public Object mapRow(ResultSet resultSet) throws SQLException {
            return resultSet.getObject(1);
        }
    };


    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> RowMapper<T> findRowMapperForType(@NotNull Class<T> cl) {
        return (RowMapper<T>) findRowMapperForTypeInternal(cl);
    }

    @Nullable
    private static RowMapper<?> findRowMapperForTypeInternal(@NotNull Class<?> cl) {
        if (cl == Boolean.class || cl == boolean.class) return BOOLEAN;
        if (cl == Short.class || cl == short.class) return SHORT;
        if (cl == Integer.class || cl == int.class) return INTEGER;
        if (cl == Long.class || cl == long.class) return LONG;
        if (cl == String.class) return STRING;
        if (cl == Object.class) return OBJECT;

        return null;
    }
}
