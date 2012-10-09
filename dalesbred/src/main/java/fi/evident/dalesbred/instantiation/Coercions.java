package fi.evident.dalesbred.instantiation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

public final class Coercions {

    private final List<Coercion<?,?>> loadCoercions = new ArrayList<Coercion<?,?>>();
    private final List<Coercion<?,Object>> storeCoercions = new ArrayList<Coercion<?,Object>>();

    @Nullable
    @SuppressWarnings("unchecked")
    public <S,T> Coercion<S,T> findCoercionFromDbValue(@NotNull Class<S> source, @NotNull Class<T> target) {
        for (Coercion<?,?> coercion : loadCoercions)
            if (coercion.canCoerce(source, target))
                return (Coercion<S,T>) coercion;

        return null;
    }

    public <S,T> void registerLoadConversion(@NotNull Coercion<S, T> coercion) {
        loadCoercions.add(requireNonNull(coercion));
    }

    @SuppressWarnings("unchecked")
    public <S,T> void registerStoreConversion(@NotNull Coercion<S, T> coercion) {
        storeCoercions.add(requireNonNull((Coercion) coercion));
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> Coercion<T,Object> findCoercionToDb(@NotNull Class<? extends T> type) {
        for (Coercion<?,?> coercion : storeCoercions)
            if (coercion.canCoerce(type, Object.class))
                return (Coercion) coercion;

        return null;
    }
}
