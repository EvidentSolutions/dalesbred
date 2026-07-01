package org.dalesbred.internal.utils;

import org.dalesbred.DatabaseException;
import org.dalesbred.internal.instantiation.InstantiationFailureException;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

public final class EnumUtils {

    private EnumUtils() { }

    public static @NotNull <T extends Enum<T>> T enumByOrdinal(@NotNull Class<T> enumType, int ordinal) {
        Enum<?>[] constants = enumType.getEnumConstants();
        if (ordinal >= 0 && ordinal < constants.length)
            return enumType.cast(constants[ordinal]);
        else
            throw new DatabaseException("invalid ordinal " + ordinal + " for enum type " + enumType.getName());
    }

    public static @NotNull <T extends Enum<T>,K> T enumByKey(@NotNull Class<T> enumType, @NotNull Function<T, K> keyFunction, @NotNull K key) {
        for (T enumConstant : enumType.getEnumConstants())
            if (Objects.equals(key, keyFunction.apply(enumConstant)))
                return enumConstant;

        throw new InstantiationFailureException("could not find enum constant of type " + enumType.getName() + " for " + key);
    }
}
