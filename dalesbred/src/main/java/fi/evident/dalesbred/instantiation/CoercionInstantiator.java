package fi.evident.dalesbred.instantiation;

import org.jetbrains.annotations.NotNull;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

/**
 * A simple instantiator that just applies a coercion to argument.
 */
final class CoercionInstantiator<T> implements Instantiator<T> {

    private final Coercion<Object, ? extends T> coercion;

    CoercionInstantiator(@NotNull Coercion<Object,? extends T> coercion) {
        this.coercion = requireNonNull(coercion);
    }

    @Override
    public T instantiate(Object[] arguments) {
        Object value = arguments[0];
        return (value != null) ? coercion.coerce(value) : null;
    }
}
