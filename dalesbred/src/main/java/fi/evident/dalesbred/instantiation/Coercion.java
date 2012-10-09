package fi.evident.dalesbred.instantiation;

import org.jetbrains.annotations.NotNull;

public abstract class Coercion<S,T> {

    private static final IdentityCoercion IDENTITY = new IdentityCoercion();

    @NotNull
    public abstract boolean canCoerce(@NotNull Class<?> source, @NotNull Class<?> target);

    @NotNull
    public abstract T coerce(@NotNull S value);

    @SuppressWarnings("unchecked")
    public static <T> Coercion<T,T> identity() {
        return IDENTITY;
    }

    private static final class IdentityCoercion<T> extends Coercion<T, T> {

        private static IdentityCoercion INSTANCE = new IdentityCoercion();

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
