package fi.evident.dalesbred.instantiation;

import fi.evident.dalesbred.DatabaseException;
import fi.evident.dalesbred.dialects.Dialect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static fi.evident.dalesbred.utils.Primitives.isAssignableByBoxing;
import static fi.evident.dalesbred.utils.Require.requireNonNull;

public final class Coercions {

    private final Dialect dialect;

    private final List<Coercion<?,?>> loadCoercions = new ArrayList<Coercion<?,?>>();
    private final List<Coercion<?,Object>> storeCoercions = new ArrayList<Coercion<?,Object>>();

    public Coercions(@NotNull Dialect dialect) {
        this.dialect = requireNonNull(dialect);
    }

    public Object[] coerceAllFromDB(Class<?>[] targetTypes, Object[] arguments) {
        if (arguments.length != targetTypes.length)
            throw new IllegalArgumentException("lengths don't match");

        Object[] result = new Object[arguments.length];

        for (int i = 0; i < arguments.length; i++)
            result[i] = coerceFromDB(targetTypes[i], arguments[i]);

        return result;
    }

    @SuppressWarnings("unchecked")
    public <T> T coerceFromDB(Class<T> targetType, Object value) {
        if (value == null || targetType.isInstance(value) || isAssignableByBoxing(targetType, value.getClass()))
            return (T) value;
        else if (targetType.isEnum())
            return (T) dialect.databaseValueToEnum((Class) targetType, value);
        else {
            Coercion coercion = findCoercionFromDbValue(value.getClass(), targetType);
            if (coercion != null)
                return (T) coercion.coerce(value);
            else
                throw new DatabaseException("can't coerce value of type " + value.getClass().getName() + " to " + targetType.getName() + " with: " + loadCoercions);
        }
    }

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
