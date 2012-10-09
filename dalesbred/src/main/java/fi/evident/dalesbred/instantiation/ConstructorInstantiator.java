package fi.evident.dalesbred.instantiation;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.List;

import static fi.evident.dalesbred.utils.Require.requireNonNull;
import static fi.evident.dalesbred.utils.Throwables.propagate;

final class ConstructorInstantiator<T> implements Instantiator<T> {

    private final Constructor<T> constructor;
    private final List<Coercion<Object,?>> coercions;

    ConstructorInstantiator(@NotNull Constructor<T> constructor, @NotNull List<Coercion<Object,?>> coercions) {
        this.constructor = requireNonNull(constructor);
        this.coercions = requireNonNull(coercions);
    }

    @Override
    public T instantiate(Object[] arguments) {
        try {
            return constructor.newInstance(coerceArguments(arguments));
        } catch (Exception e) {
            throw propagate(e);
        }
    }

    private Object[] coerceArguments(Object[] arguments) {
        Object[] coerced = new Object[arguments.length];

        for (int i = 0; i < arguments.length; i++)
            coerced[i] = coercions.get(i).coerce(arguments[i]);

        return coerced;
    }
}
