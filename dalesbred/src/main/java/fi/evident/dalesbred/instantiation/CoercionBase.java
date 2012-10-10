package fi.evident.dalesbred.instantiation;

import org.jetbrains.annotations.NotNull;

import static fi.evident.dalesbred.utils.Primitives.isAssignableByBoxing;
import static fi.evident.dalesbred.utils.Require.requireNonNull;

public abstract class CoercionBase<S,T> extends Coercion<S,T> {

    private final Class<S> source;
    private final Class<T> target;

    protected CoercionBase(@NotNull Class<S> source, @NotNull Class<T> target) {
        this.source = requireNonNull(source);
        this.target = requireNonNull(target);
    }

    @NotNull
    @Override
    public boolean canCoerce(@NotNull Class<?> source, @NotNull Class<?> target) {
        return isAssignableByBoxing(this.source, source) && isAssignableByBoxing(target, this.target);
    }

    @Override
    public String toString() {
        return getClass().getName() + " [" + source.getName() + " -> " + target.getName() + "]";
    }
}
