package org.dalesbred.internal.instantiation;

import org.dalesbred.internal.utils.Throwables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * An instantiator that uses constructor and setters or fields to instantiate an object.
 */
final class ReflectionInstantiator<T> implements Instantiator<T> {

    private final @NotNull Executable instantiator;
    private final @NotNull List<TypeConversion> conversions;

    private final @NotNull List<PropertyAccessor> accessors;

    private final int parameterCount;

    ReflectionInstantiator(@NotNull Executable instantiator,
                           @NotNull List<TypeConversion> conversions,
                           @NotNull List<PropertyAccessor> accessors) {
        this.instantiator = requireNonNull(instantiator);
        this.conversions = requireNonNull(conversions);
        this.accessors = requireNonNull(accessors);
        this.parameterCount = instantiator.getParameterTypes().length;
    }


    @Override
    public @Nullable T instantiate(@NotNull InstantiatorArguments arguments) {
        try {
            Object[] argumentArray = toArgumentArray(arguments.getValues());

            @Nullable Object v;
            if (instantiator instanceof Constructor<?>) {
                v = ((Constructor<?>) instantiator).newInstance(argumentArray);
            } else if (instantiator instanceof  Method) {
                v = ((Method) instantiator).invoke(null, argumentArray);
            } else {
                throw new IllegalStateException("Unexpected instantiator: " + instantiator);
            }

            @SuppressWarnings("unchecked")
            T value = (T) v;
            if (value != null)
                bindRemainingProperties(value, arguments);
            return value;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private void bindRemainingProperties(@NotNull T result, @NotNull InstantiatorArguments arguments) {
        List<?> values = arguments.getValues();

        for (int i = 0, len = accessors.size(); i < len; i++) {
            int argumentIndex = i + parameterCount;
            Object originalValue = values.get(argumentIndex);
            Object convertedValue = conversions.get(argumentIndex).convert(originalValue);
            accessors.get(i).set(result, convertedValue);
        }
    }

    private @NotNull Object[] toArgumentArray(@NotNull List<?> arguments) {
        Object[] result = new Object[parameterCount];

        for (int i = 0; i < result.length; i++)
            result[i] = conversions.get(i).convert(arguments.get(i));

        return result;
    }
}
