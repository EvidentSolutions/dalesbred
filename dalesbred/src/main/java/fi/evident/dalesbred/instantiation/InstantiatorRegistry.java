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
    private final Coercions coercions = new Coercions();
    private static final Logger log = Logger.getLogger(InstantiatorRegistry.class.getName());

    public InstantiatorRegistry(@NotNull Dialect dialect) {
        this.dialect = requireNonNull(dialect);

        DefaultCoercions.register(coercions);

        if (JodaCoercions.hasJoda()) {
            log.fine("Detected Joda Time in classpath. Registering coercions for Joda.");
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
            Coercion<Object, ? extends T> coercion = (Coercion) findCoercionFromDbValue(types.getType(0), cl);
            if (coercion != null)
                return new CoercionInstantiator<T>(coercion);
        }

        // If there was no coercion, we try to find a matching constructor, applying coercions to arguments.
        for (Constructor<T> constructor : constructorsFor(cl)) {
            Instantiator<T> instantiator = instantiatorFrom(constructor, types);
            if (instantiator != null)
                return instantiator;
        }

        throw new DatabaseException(cl + " does not have instantiator matching types " + types);
    }

    /**
     * Returns an instantiator that uses given constructor and given types to create instances,
     * or null if there are no coercions that can be made to instantiate the type.
     */
    @Nullable
    private <T> Instantiator<T> instantiatorFrom(Constructor<T> constructor, NamedTypeList types) {
        if (!isPublic(constructor.getModifiers())) return null;

        List<Coercion<Object,?>> coercions = resolveCoercions(types, constructor.getParameterTypes());
        if (coercions != null)
            return new ConstructorInstantiator<T>(constructor, coercions);
        else
            return null;
    }

    /**
     * Returns the list of coercions that need to be performed to convert sourceTypes
     * to targetTypes, or null if coercions can't be done.
     */
    @Nullable
    private List<Coercion<Object,?>> resolveCoercions(@NotNull NamedTypeList sourceTypes,
                                                      @NotNull Class<?>[] targetTypes) {
        if (targetTypes.length != sourceTypes.size())
            return null;

        List<Coercion<Object,?>> coercions = new ArrayList<Coercion<Object, ?>>(targetTypes.length);

        for (int i = 0; i < targetTypes.length; i++) {
            @SuppressWarnings("unchecked")
            Coercion<Object,?> coercion = (Coercion) findCoercionFromDbValue(sourceTypes.getType(i), targetTypes[i]);
            if (coercion != null)
                coercions.add(coercion);
            else
                return null;
        }

        return coercions;
    }

    /**
     * Returns coercion for converting value of source-type to target-type, or throws exception if
     * there's no such coercion.
     */
    @NotNull
    public <S,T> Coercion<? super S, ? extends T> getCoercionFromDbValue(@NotNull Class<S> source, @NotNull Class<T> target) {
        Coercion<? super S, ? extends T> coercion = findCoercionFromDbValue(source, target);
        if (coercion != null)
            return coercion;
        else
            throw new DatabaseException("could not find a conversion from " + source.getName() + " to " + target.getName());
    }

    /**
     * Returns coercion for converting value of source to target, or returns null if there's no such coercion.
     */
    @Nullable
    private <S,T> Coercion<? super S, ? extends T> findCoercionFromDbValue(@NotNull Class<S> source, @NotNull Class<T> target) {
        if (wrap(target).isAssignableFrom(wrap(source)))
            return Coercion.identity();

        Coercion<?,?> coercion = coercions.findCoercionFromDbValue(source, target);
        if (coercion != null)
            return coercion.cast(source, target);

        if (target.isEnum())
            return dialect.getEnumCoercion(target.asSubclass(Enum.class)).cast(source, target);

        return null;
    }

    @NotNull
    private static <T> Constructor<T>[] constructorsFor(@NotNull Class<T> cl) {
        @SuppressWarnings("unchecked")
        Constructor<T>[] constructors = (Constructor<T>[]) cl.getConstructors();
        return constructors;
    }
}
