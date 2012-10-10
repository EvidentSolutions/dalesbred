package fi.evident.dalesbred.instantiation;

import org.jetbrains.annotations.NotNull;

public abstract class Coercion<S,T> {

    private static final IdentityCoercion IDENTITY = new IdentityCoercion();

    @NotNull
    public abstract boolean canCoerce(@NotNull Class<?> source, @NotNull Class<?> target);

    @NotNull
    public abstract T coerce(@NotNull S value);

    @SuppressWarnings("unchecked")
    public static <S,T> Coercion<S,T> identity() {
        return IDENTITY;
    }

    @SuppressWarnings("unchecked")
    public <S,T> Coercion<S,T> cast(Class<S> source, Class<T> target) {
        if (canCoerce(source, target))
            return (Coercion) this;
        else
            throw new RuntimeException("can't cast " + this + " to coercion from " + source.getName() + " to " + target.getName());
    }

    private static final class IdentityCoercion<T> extends Coercion<T, T> {

        @NotNull
        @Override
        public boolean canCoerce(@NotNull Class<?> source, @NotNull Class<?> target) {
            return true;
        }

        @NotNull
        @Override
        public T coerce(@NotNull T value) {
            return value;
        }
    }
}
