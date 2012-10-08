package fi.evident.dalesbred.results;

import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

public final class EnumRowMapper<T extends Enum<T>> implements RowMapper<T> {

    private final Class<T> enumType;

    public EnumRowMapper(@NotNull Class<T> enumType) {
        this.enumType = requireNonNull(enumType);
    }

    @Override
    public T mapRow(ResultSet resultSet) throws SQLException {
        // TODO: this assumes that DB returns enums as strings
        String value = resultSet.getString(1);
        if (value == null)
            return null;

        return Enum.valueOf(enumType, value);
    }
}
