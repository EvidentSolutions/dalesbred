package fi.evident.dalesbred.instantiation;

import fi.evident.dalesbred.DatabaseException;

import java.lang.reflect.Constructor;

import static fi.evident.dalesbred.utils.Primitives.isAssignableByBoxing;
import static fi.evident.dalesbred.utils.Require.requireNonNull;
import static fi.evident.dalesbred.utils.Throwables.propagate;

final class ConstructorInstantiator<T> implements Instantiator<T> {

    private final Constructor<T> constructor;
    private final int cost;

    ConstructorInstantiator(Constructor<T> constructor, int cost) {
        this.constructor = requireNonNull(constructor);
        this.cost = cost;
    }

    @Override
    public T instantiate(Object[] arguments) {
        try {
            Object[] coerced = coerceArguments(constructor.getParameterTypes(), arguments);
            return constructor.newInstance(coerced);
        } catch (Exception e) {
            throw propagate(e);
        }
    }

    private static Object[] coerceArguments(Class<?>[] types, Object[] arguments) {
        Object[] result = new Object[arguments.length];

        for (int i = 0; i < arguments.length; i++)
            result[i] = coerce(types[i], arguments[i]);

        return result;
    }

    private static Object coerce(Class<?> type, Object value) {
        if (value == null || type.isInstance(value) || isAssignableByBoxing(type, value.getClass()))
            return value;
        else if (type.isEnum())
            return parseEnum(type, value.toString());
        else
            throw new DatabaseException("can't coerce value of type " + value.getClass().getName() + " to " + type.getName());
    }

    @SuppressWarnings("unchecked")
    private static Object parseEnum(Class cl, String name) {
        return Enum.valueOf(cl, name);
    }

    @Override
    public int getCost() {
        return cost;
    }
}
