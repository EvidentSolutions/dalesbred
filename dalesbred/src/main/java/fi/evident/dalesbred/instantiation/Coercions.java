package fi.evident.dalesbred.instantiation;

import fi.evident.dalesbred.DatabaseException;
import fi.evident.dalesbred.dialects.Dialect;
import org.jetbrains.annotations.NotNull;

import static fi.evident.dalesbred.utils.Primitives.isAssignableByBoxing;
import static fi.evident.dalesbred.utils.Require.requireNonNull;

public final class Coercions {

    private final Dialect dialect;

    public Coercions(@NotNull Dialect dialect) {
        this.dialect = requireNonNull(dialect);
    }

    public Object[] coerceAllFromDB(Class<?>[] targetTypes, Object[] arguments) {
        if (arguments.length != targetTypes.length)
            throw new IllegalArgumentException("lengths don't match");

        Object[] result = new Object[arguments.length];

        for (int i = 0; i < arguments.length; i++)
            result[i] = coerceFromDB(targetTypes[i], arguments[i]);

        return result;
    }

    @SuppressWarnings("unchecked")
    public <T> T coerceFromDB(Class<T> targetType, Object value) {
        if (value == null || targetType.isInstance(value) || isAssignableByBoxing(targetType, value.getClass()))
            return (T) value;
        else if (targetType.isEnum())
            return (T) dialect.databaseValueToEnum((Class) targetType, value);
        else
            throw new DatabaseException("can't coerce value of type " + value.getClass().getName() + " to " + targetType.getName());
    }
}
