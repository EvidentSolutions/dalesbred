package org.dalesbred.internal.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class OptionalUtils {

    private OptionalUtils() {
    }

    /**
     * If object is an empty optional-type, return null. If object is non-empty
     * optional-type, return its value. Otherwise return object as it is.
     */
    public static @Nullable Object unwrapOptionalAsNull(@Nullable Object o) {
        return o instanceof Optional<?> ? unwrap((Optional<?>) o)
                : o instanceof OptionalInt ? unwrap((OptionalInt) o)
                : o instanceof OptionalLong ? unwrap((OptionalLong) o)
                : o instanceof OptionalDouble ? unwrap((OptionalDouble) o)
                : o;
    }

    private static @Nullable <T> T unwrap(@NotNull Optional<T> o) {
        return o.orElse(null);
    }

    private static @Nullable Object unwrap(@NotNull OptionalInt o) {
        return o.isPresent() ? o.getAsInt() : null;
    }

    private static @Nullable Object unwrap(@NotNull OptionalLong o) {
        return o.isPresent() ? o.getAsLong() : null;
    }

    private static @Nullable Object unwrap(@NotNull OptionalDouble o) {
        return o.isPresent() ? o.getAsDouble() : null;
    }

    public static @NotNull OptionalInt optionalIntOfNullable(@Nullable Integer v) {
        return v != null ? OptionalInt.of(v) : OptionalInt.empty();
    }

    public static @NotNull OptionalLong optionalLongOfNullable(@Nullable Long v) {
        return v != null ? OptionalLong.of(v) : OptionalLong.empty();
    }

    public static @NotNull OptionalDouble optionalDoubleOfNullable(@Nullable Double v) {
        return v != null ? OptionalDouble.of(v) : OptionalDouble.empty();
    }
}
