package org.dalesbred.query;

import org.dalesbred.internal.utils.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Resolves values for variables appearing in SQL queries with named parameters.
 */
@FunctionalInterface
public interface VariableResolver {

    /**
     * Returns the value of given variable, which could be null.
     *
     * @throws VariableResolutionException if resolution fails
     */
    @Nullable
    Object getValue(@NotNull String variable);

    /**
     * Returns a {@link VariableResolver} that is backed by given map.
     */
    static @NotNull VariableResolver forMap(@NotNull Map<String, ?> variables) {
        return variable -> {
            Object value = variables.get(variable);
            if (value != null || variables.containsKey(variable))
                return value;
            else
                throw new VariableResolutionException("No value registered for key '" + variable + '\'');
        };
    }

    /**
     * Returns a {@link VariableResolver} that is backed by given bean. When variables are looked up,
     * tries to find a matching getter or accessible field for the variable and returns its value.
     */
    static @NotNull VariableResolver forBean(@NotNull Object object) {
        return variable -> {
            try {
                Method getter = ReflectionUtils.findGetter(object.getClass(), variable).orElse(null);
                if (getter != null)
                    return getter.invoke(object);

                Field field = ReflectionUtils.findField(object.getClass(), variable).orElse(null);
                if (field != null)
                    return field.get(object);

                throw new VariableResolutionException("No accessor found for '" + variable + '\'');
            } catch (InvocationTargetException e) {
                throw new VariableResolutionException("Failed to resolve variable '" + variable + "': " + e.getTargetException(), e.getTargetException());
            } catch (IllegalAccessException e) {
                throw new VariableResolutionException("Could not access variable'" + variable + '\'', e);
            }
        };
    }
}
