package fi.evident.dalesbred.instantiation;

import org.jetbrains.annotations.NotNull;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

final class CoercionInstantiator<T> implements Instantiator<T> {

    private final Coercion<Object,T> coercion;

    CoercionInstantiator(@NotNull Coercion<Object,T> coercion) {
        this.coercion = requireNonNull(coercion);
    }

    @Override
    public T instantiate(Object[] arguments) {
        Object value = arguments[0];
        return (value != null) ? coercion.coerce(value) : null;
    }
}
