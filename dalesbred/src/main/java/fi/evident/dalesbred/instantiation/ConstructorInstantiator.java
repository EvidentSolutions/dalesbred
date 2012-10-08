package fi.evident.dalesbred.instantiation;

import java.lang.reflect.Constructor;

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
            return constructor.newInstance(arguments);
        } catch (Exception e) {
            throw propagate(e);
        }
    }

    @Override
    public int getCost() {
        return cost;
    }
}
