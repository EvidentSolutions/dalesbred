package fi.evident.dalesbred.instantiation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

public final class Coercions {

    private final List<Coercion<?,?>> loadCoercions = new ArrayList<Coercion<?,?>>();
    private final List<Coercion<?,?>> storeCoercions = new ArrayList<Coercion<?,?>>();

    @Nullable
    public <S,T> Coercion<S,T> findCoercionFromDbValue(@NotNull Class<S> source, @NotNull Class<T> target) {
        for (Coercion<?,?> coercion : loadCoercions)
            if (coercion.canCoerce(source, target))
                return coercion.cast(source, target);

        return null;
    }

    public <S,T> void registerLoadConversion(@NotNull Coercion<S, T> coercion) {
        loadCoercions.add(requireNonNull(coercion));
    }

    public <S,T> void registerStoreConversion(@NotNull Coercion<S, T> coercion) {
        storeCoercions.add(requireNonNull(coercion));
    }

    @Nullable
    public <T> Coercion<T,Object> findCoercionToDb(@NotNull Class<T> type) {
        for (Coercion<?,?> coercion : storeCoercions)
            if (coercion.canCoerce(type, Object.class))
                return coercion.cast(type, Object.class);

        return null;
    }
}
