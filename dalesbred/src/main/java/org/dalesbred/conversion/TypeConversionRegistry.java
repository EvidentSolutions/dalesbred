package org.dalesbred.conversion;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Registry containing the type-conversions used when converting database values
 * to model values and vice versa.
 */
public interface TypeConversionRegistry {

    /**
     * Registers conversion from given source database type to given target model type.
     */
    <S, T> void registerConversionFromDatabase(@NotNull Class<S> source, @NotNull Class<T> target, @NotNull Function<S, T> conversion);

    /**
     * Registers conversion from given source model type to database type.
     */
    <T> void registerConversionToDatabase(@NotNull Class<T> source, @NotNull Function<T, ?> conversion);

    /**
     * Registers conversions from database type to model type and back.
     */
    default <D, J> void registerConversions(@NotNull Class<D> databaseType,
                                            @NotNull Class<J> javaType,
                                            @NotNull Function<D, J> fromDatabase,
                                            @NotNull Function<J, D> toDatabase) {
        registerConversionFromDatabase(databaseType, javaType, fromDatabase);
        registerConversionToDatabase(javaType, toDatabase);
    }

    /**
     * Registers simple enum conversion that uses keyFunction to produce saved value and uses
     * same function on enum constants to convert values back.
     */
    <T extends Enum<T>,K> void registerEnumConversion(@NotNull Class<T> enumType, @NotNull Function<T,K> keyFunction);

    /**
     * Registers the given enum type to be persisted as a database-native enum of the given type name.
     */
    default <T extends Enum<T>> void registerNativeEnumConversion(@NotNull Class<T> enumType, @NotNull String typeName) {
        registerNativeEnumConversion(enumType, typeName, Enum::name);
    }

    /**
     * Registers the given enum type to be persisted as a database-native enum of the given type name.
     * The given function maps each enum constant to the stored value.
     */
    <T extends Enum<T>, K> void registerNativeEnumConversion(@NotNull Class<T> enumType, @NotNull String typeName, @NotNull Function<T,K> keyFunction);
}
