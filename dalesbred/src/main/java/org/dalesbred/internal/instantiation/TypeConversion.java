package org.dalesbred.internal.instantiation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * A conversion from S into T.
 */
public class TypeConversion {

    private final @NotNull Function<Object,Object> conversion;

    @SuppressWarnings("unchecked")
    private TypeConversion(@NotNull Function<?, ?> conversion) {
        this.conversion = (Function<Object,Object>) conversion;
    }

    public static @NotNull <S,T> TypeConversion fromNonNullFunction(@NotNull Function<S, T> function) {
        return new TypeConversion((S value) -> value != null ? function.apply(value) : null);
    }

    /**
     * Returns identity-conversion, ie. a conversion that does nothing.
     */
    public static @NotNull TypeConversion identity() {
        return new TypeConversion(Function.identity());
    }

    public @Nullable Object convert(@Nullable Object value) {
        return conversion.apply(value);
    }

    @SuppressWarnings("unchecked")
    public @NotNull TypeConversion compose(@NotNull Function<?,?> function) {
        return new TypeConversion(conversion.andThen((Function<Object,Object>) function));
    }
}
