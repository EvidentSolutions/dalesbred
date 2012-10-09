package fi.evident.dalesbred.instantiation;

import fi.evident.dalesbred.dialects.Dialect;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.logging.Logger;

import static fi.evident.dalesbred.utils.Primitives.unwrap;
import static fi.evident.dalesbred.utils.Primitives.wrap;
import static java.lang.reflect.Modifier.isPublic;

/**
 * Provides {@link Instantiator}s for classes.
 */
public final class InstantiatorRegistry {

    private final Coercions coercions;
    private static final Logger log = Logger.getLogger(InstantiatorRegistry.class.getName());
    private static final int SAME_COST = 0;
    private static final int SUBTYPE_COST = 1;
    private static final int BOXING_COST = 100;
    private static final int UNBOXING_COST = 101;
    private static final int ENUM_COST = 200;
    private static final int COERCION_COST = 300;
    private static final int NO_MATCH_COST = Integer.MAX_VALUE;

    public InstantiatorRegistry(@NotNull Dialect dialect) {
        this.coercions = new Coercions(dialect);

        if (JodaCoercions.hasJoda()) {
            log.info("Detected Joda Time in classpath. Registering coercions for Joda.");
            JodaCoercions.register(coercions);
        }
    }

    @NotNull
    public Coercions getCoercions() {
        return coercions;
    }

    /**
     * Returns constructor matching given argument types. Differs from 
     * {@link Class#getConstructor(Class[])} in that this method allows
     * does not require strict match for types, but finds any constructor
     * that is assignable from given types.
     */
    public <T> Instantiator<T> findInstantiator(Class<T> cl, NamedTypeList types) throws NoSuchMethodException {
        Instantiator<T> best = null;

        for (Constructor<T> constructor : constructorsFor(cl)) {
            Instantiator<T> instantiator = instantiatorFrom(constructor, types);
            if (instantiator != null && (best == null || instantiator.getCost() < best.getCost()))
                best = instantiator;
        }

        if (best != null)
            return best;
        else
            throw new NoSuchMethodException(cl + " does not have constructor matching types " + types.toString());
    }

    private <T> Instantiator<T> instantiatorFrom(Constructor<T> constructor, NamedTypeList types) {
        if (!isPublic(constructor.getModifiers())) return null;
        
        int cost = cost(constructor, types);
        if (cost != NO_MATCH_COST)
            return new ConstructorInstantiator<T>(constructor, coercions, cost);
        else
            return null;
    }

    private int cost(Constructor<?> constructor, NamedTypeList columnTypes) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();

        if (parameterTypes.length != columnTypes.size())
            return NO_MATCH_COST;

        int totalCost = 0;
        for (int i = 0; i < parameterTypes.length; i++) {
            int assignScore = assignmentCost(parameterTypes[i], columnTypes.getType(i));
            if (assignScore == NO_MATCH_COST)
                return NO_MATCH_COST;
            else
                totalCost += assignScore;
        }

        return totalCost;
    }

    private int assignmentCost(Class<?> target, Class<?> source) {
        return target == source                               ? SAME_COST
             : target.isAssignableFrom(source)                ? SUBTYPE_COST
             : target.isAssignableFrom(wrap(source))          ? BOXING_COST
             : target.isAssignableFrom(unwrap(source))        ? UNBOXING_COST
             : target.isEnum()                                ? ENUM_COST
             : coercions.findCoercion(source, target) != null ? COERCION_COST
             : NO_MATCH_COST;
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T>[] constructorsFor(Class<T> cl) {
        return (Constructor<T>[]) cl.getConstructors();
    }
}
