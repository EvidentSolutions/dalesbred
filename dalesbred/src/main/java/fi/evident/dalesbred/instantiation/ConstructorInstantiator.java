package fi.evident.dalesbred.instantiation;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

import static fi.evident.dalesbred.utils.Require.requireNonNull;
import static fi.evident.dalesbred.utils.Throwables.propagate;

final class ConstructorInstantiator<T> implements Instantiator<T> {

    private final Constructor<T> constructor;
    private final Coercions coercions;
    private final int cost;

    ConstructorInstantiator(@NotNull Constructor<T> constructor, @NotNull Coercions coercions, int cost) {
        this.constructor = requireNonNull(constructor);
        this.coercions = requireNonNull(coercions);
        this.cost = cost;
    }

    @Override
    public T instantiate(Object[] arguments) {
        try {
            Object[] coerced = coercions.coerceAllFromDB(constructor.getParameterTypes(), arguments);
            return constructor.newInstance(coerced);
        } catch (Exception e) {
            throw propagate(e);
        }
    }

    @Override
    public int getCost() {
        return cost;
    }
}
