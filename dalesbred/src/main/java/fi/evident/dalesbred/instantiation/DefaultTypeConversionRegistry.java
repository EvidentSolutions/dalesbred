package fi.evident.dalesbred.instantiation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

/**
 * The used implementation of TypeConversionRegistry.
 */
final class DefaultTypeConversionRegistry implements TypeConversionRegistry {

    private final List<TypeConversion<?,?>> loadCoercions = new ArrayList<TypeConversion<?,?>>();
    private final List<TypeConversion<?,?>> storeCoercions = new ArrayList<TypeConversion<?,?>>();

    @Nullable
    public <S,T> TypeConversion<S,T> findCoercionFromDbValue(@NotNull Class<S> source, @NotNull Class<T> target) {
        for (TypeConversion<?,?> coercion : loadCoercions)
            if (coercion.canConvert(source, target))
                return coercion.cast(source, target);

        return null;
    }

    @Nullable
    public <T> TypeConversion<T,Object> findCoercionToDb(@NotNull Class<T> type) {
        for (TypeConversion<?,?> coercion : storeCoercions)
            if (coercion.canConvert(type, Object.class))
                return coercion.cast(type, Object.class);

        return null;
    }

    @Override
    public void registerConversionFromDatabaseType(@NotNull TypeConversion<?, ?> coercion) {
        loadCoercions.add(requireNonNull(coercion));
    }

    @Override
    public void registerConversionToDatabaseType(@NotNull TypeConversion<?, ?> coercion) {
        storeCoercions.add(requireNonNull(coercion));
    }
}
