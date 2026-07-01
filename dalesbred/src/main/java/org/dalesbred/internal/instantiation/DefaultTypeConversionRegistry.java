package org.dalesbred.internal.instantiation;

import org.dalesbred.conversion.TypeConversionPair;
import org.dalesbred.conversion.TypeConversionRegistry;
import org.dalesbred.dialect.Dialect;
import org.dalesbred.internal.utils.EnumUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;

/**
 * The used implementation of TypeConversionRegistry.
 */
final class DefaultTypeConversionRegistry implements TypeConversionRegistry {

    private final @NotNull Dialect dialect;

    private final @NotNull ConversionMap loadConversions = new ConversionMap();

    private final @NotNull ConversionMap storeConversions = new ConversionMap();

    public DefaultTypeConversionRegistry(@NotNull Dialect dialect) {
        this.dialect = dialect;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Enum<T>,K> void registerEnumConversion(@NotNull Class<T> enumType, @NotNull Function<T, K> keyFunction) {
        registerConversionFromDatabase(Object.class, enumType, value -> EnumUtils.enumByKey(enumType, keyFunction, (K) value));
        registerConversionToDatabase(enumType, keyFunction);
    }

    @Override
    public <T extends Enum<T>, K> void registerNativeEnumConversion(@NotNull Class<T> enumType, @NotNull String typeName, @NotNull Function<T,K> keyFunction) {
        TypeConversionPair<Object, T> conversions = dialect.createNativeEnumConversions(enumType, typeName, keyFunction);
        registerConversions(Object.class, enumType, conversions::convertFromDatabase, conversions::convertToDatabase);
    }

    public @NotNull Optional<TypeConversion> findConversionFromDbValue(@NotNull Type source, @NotNull Type target) {
        return loadConversions.findConversion(source, target);
    }

    public @NotNull Optional<TypeConversion> findConversionToDb(@NotNull Type type) {
        return storeConversions.findConversion(type, Object.class);
    }

    @Override
    public <S, T> void registerConversionFromDatabase(@NotNull Class<S> source, @NotNull Class<T> target, @NotNull Function<S, T> conversion) {
        loadConversions.register(source, target, TypeConversion.fromNonNullFunction(conversion));
    }

    @Override
    public <S> void registerConversionToDatabase(@NotNull Class<S> source, @NotNull Function<S, ?> conversion) {
        storeConversions.register(source, Object.class, TypeConversion.fromNonNullFunction(conversion));
    }
}
