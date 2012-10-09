package fi.evident.dalesbred.instantiation;

import org.jetbrains.annotations.NotNull;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

public abstract class Coercion<S,T> {

    final Class<S> source;
    final Class<T> target;

    protected Coercion(@NotNull Class<S> source, @NotNull Class<T> target) {
        this.source = requireNonNull(source);
        this.target = requireNonNull(target);
    }

    public final boolean canCoerce(Class<?> source, Class<?> target) {
        return this.source.isAssignableFrom(source) && target.isAssignableFrom(this.target);
    }

    @NotNull
    public abstract T coerce(@NotNull S value);

    @Override
    public String toString() {
        return getClass().getName() + " [" + source.getName() + " -> " + target.getName() + "]";
    }
}
