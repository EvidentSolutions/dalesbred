package fi.evident.dalesbred.instantiation;

import fi.evident.dalesbred.DatabaseException;

import static fi.evident.dalesbred.utils.Primitives.isAssignableByBoxing;

public class Coercions {
    public Object[] coerceAll(Class<?>[] targetTypes, Object[] arguments) {
        if (arguments.length != targetTypes.length)
            throw new IllegalArgumentException("lengths don't match");

        Object[] result = new Object[arguments.length];

        for (int i = 0; i < arguments.length; i++)
            result[i] = coerce(targetTypes[i], arguments[i]);

        return result;
    }

    @SuppressWarnings("unchecked")
    public <T> T coerce(Class<T> type, Object value) {
        if (value == null || type.isInstance(value) || isAssignableByBoxing(type, value.getClass()))
            return (T) value;
        else if (type.isEnum())
            return (T) Enum.valueOf((Class) type, value.toString());
        else
            throw new DatabaseException("can't coerce value of type " + value.getClass().getName() + " to " + type.getName());
    }
}
