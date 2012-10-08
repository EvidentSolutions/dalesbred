package fi.evident.dalesbred.results;

import fi.evident.dalesbred.instantiation.Coercions;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

public final class EnumRowMapper<T extends Enum<T>> implements RowMapper<T> {

    private final Class<T> enumType;
    private static final Coercions coercions = new Coercions();

    public EnumRowMapper(@NotNull Class<T> enumType) {
        this.enumType = requireNonNull(enumType);
    }

    @Override
    public T mapRow(ResultSet resultSet) throws SQLException {
        return coercions.coerce(enumType, resultSet.getObject(1));
    }
}
