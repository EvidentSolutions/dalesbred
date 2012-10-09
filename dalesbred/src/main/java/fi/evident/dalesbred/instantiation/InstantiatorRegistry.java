package fi.evident.dalesbred.instantiation;

import fi.evident.dalesbred.DatabaseException;
import fi.evident.dalesbred.dialects.Dialect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static fi.evident.dalesbred.utils.Primitives.wrap;
import static fi.evident.dalesbred.utils.Require.requireNonNull;
import static java.lang.reflect.Modifier.isPublic;

/**
 * Provides {@link Instantiator}s for classes.
 */
public final class InstantiatorRegistry {

    private final Dialect dialect;
    private final Coercions coercions;
    private static final Logger log = Logger.getLogger(InstantiatorRegistry.class.getName());

    public InstantiatorRegistry(@NotNull Dialect dialect) {
        this.dialect = requireNonNull(dialect);
        this.coercions = new Coercions(dialect);

        if (JodaCoercions.hasJoda()) {
            log.info("Detected Joda Time in classpath. Registering coercions for Joda.");
            JodaCoercions.register(coercions);
        }
    }

    @Nullable
    public Object valueToDatabase(@Nullable Object value) {
        if (value == null) return null;

        Coercion<Object, Object> coercion = coercions.findCoercionToDb(value.getClass());
        if (coercion != null)
            return coercion.coerce(value);
        else
            return dialect.valueToDatabase(value);
    }

    /**
     * Returns constructor matching given argument types. Differs from 
     * {@link Class#getConstructor(Class[])} in that this method allows
     * does not require strict match for types, but finds any constructor
     * that is assignable from given types.
     */
    public <T> Instantiator<T> findInstantiator(Class<T> cl, NamedTypeList types) {
        // First check if we have an immediate coercion registered. If so, we'll just use that.
        if (types.size() == 1) {
            @SuppressWarnings("unchecked")
            Coercion<Object, T> coercion = (Coercion) findCoercionFromDbValue(cl, types.getType(0));
            if (coercion != null) {
                return new CoercionInstantiator<T>(coercion);
            }
        }

        // If there was no coercion, we try to find a matching constructor, applying coercions to arguments.
        Instantiator<T> instantiator = null;

        for (Constructor<T> constructor : constructorsFor(cl)) {
            instantiator = instantiatorFrom(constructor, types);
            if (instantiator != null)
                break;
        }

        if (instantiator != null)
            return instantiator;
        else
            throw new DatabaseException(cl + " does not have constructor matching types " + types.toString());
    }

    @Nullable
    private <T> Instantiator<T> instantiatorFrom(Constructor<T> constructor, NamedTypeList types) {
        if (!isPublic(constructor.getModifiers())) return null;

        List<Coercion<Object,?>> coercions = resolveCoercions(constructor, types);
        if (coercions != null)
            return new ConstructorInstantiator<T>(constructor, coercions);
        else
            return null;
    }

    @Nullable
    private List<Coercion<Object,?>> resolveCoercions(Constructor<?> constructor, NamedTypeList columnTypes) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();

        if (parameterTypes.length != columnTypes.size())
            return null;

        List<Coercion<Object,?>> coercions = new ArrayList<Coercion<Object, ?>>(parameterTypes.length);

        for (int i = 0; i < parameterTypes.length; i++) {
            @SuppressWarnings("unchecked")
            Coercion<Object,?> coercion = (Coercion) findCoercionFromDbValue(parameterTypes[i], columnTypes.getType(i));
            if (coercion != null)
                coercions.add(coercion);
            else
                return null;
        }

        return coercions;
    }

    @NotNull
    public <S,T> Coercion<S,T> getCoercionFromDbValue(@NotNull Class<S> source, @NotNull Class<T> target) {
        Coercion<S,T> coercion = findCoercionFromDbValue(source, target);
        if (coercion != null)
            return coercion;
        else
            throw new DatabaseException("could not find a conversion from " + source.getName() + " to " + target.getName());
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <S,T> Coercion<S,T> findCoercionFromDbValue(@NotNull Class<S> target, @NotNull Class<T> source) {
        if (wrap(target).isAssignableFrom(wrap(source)))
            return (Coercion) Coercion.identity();

        @SuppressWarnings("unchecked")
        Coercion<S,T> coercion = (Coercion) coercions.findCoercionFromDbValue(source, target);
        if (coercion != null)
            return coercion;

        if (target.isEnum())
            return (Coercion) dialect.getEnumCoercion(target.asSubclass(Enum.class));

        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T>[] constructorsFor(Class<T> cl) {
        return (Constructor<T>[]) cl.getConstructors();
    }
}
