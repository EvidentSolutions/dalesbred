package fi.evident.dalesbred.results;

import fi.evident.dalesbred.instantiation.Coercions;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

public final class EnumRowMapper<T extends Enum<T>> implements RowMapper<T> {

    private final Class<T> enumType;
    private final Coercions coercions;

    public EnumRowMapper(@NotNull Class<T> enumType, @NotNull Coercions coercions) {
        this.enumType = requireNonNull(enumType);
        this.coercions = requireNonNull(coercions);
    }

    @Override
    public T mapRow(ResultSet resultSet) throws SQLException {
        return coercions.coerceFromDB(enumType, resultSet.getObject(1));
    }
}
