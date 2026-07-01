package org.dalesbred.internal.utils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.Type;

/**
 * Utilities for handling primitive types and their wrappers uniformly.
 */
public final class Primitives {

    private Primitives() { }

    /**
     * Returns the corresponding wrapper type for a primitive type, or the type itself if it is not a primitive type.
     */
    public static @NotNull <T> Class<T> wrap(@NotNull Class<T> type) {
        Class<?> result =
               (type == boolean.class) ? Boolean.class
             : (type == byte.class)    ? Byte.class
             : (type == char.class)    ? Character.class
             : (type == short.class)   ? Short.class
             : (type == int.class)     ? Integer.class
             : (type == long.class)    ? Long.class
             : (type == float.class)   ? Float.class
             : (type == double.class)  ? Double.class
             : type;

        @SuppressWarnings("unchecked")
        Class<T> casted = (Class<T>) result;
        return casted;
    }

    public static @NotNull Type wrap(@NotNull Type type) {
        if (type instanceof Class<?>)
            return wrap((Class<?>) type);
        else
            return type;
    }

    /**
     * Returns the corresponding primitive type for a wrapper type, or the type itself if it is not a wrapper.
     */
    public static @NotNull <T> Class<T> unwrap(@NotNull Class<T> type) {
        Class<?> result =
               (type == Boolean.class)   ? boolean.class
             : (type == Byte.class)      ? byte.class
             : (type == Character.class) ? char.class
             : (type == Short.class)     ? short.class
             : (type == Integer.class)   ? int.class
             : (type == Long.class)      ? long.class
             : (type == Float.class)     ? float.class
             : (type == Double.class)    ? double.class
             : type;

        @SuppressWarnings("unchecked")
        Class<T> casted = (Class<T>) result;
        return casted;
    }

    public static @NotNull Object[] arrayAsObjectArray(@NotNull Object o) {
        Class<?> type = o.getClass();
        if (!type.isArray()) throw new IllegalArgumentException("not an array: " + o);

        if (o instanceof Object[])
            return (Object[]) o;

        int length = Array.getLength(o);
        Object[] result = (Object[]) Array.newInstance(wrap(type.getComponentType()), length);

        for (int i = 0; i < length; i++)
            result[i] = Array.get(o, i);

        return result;
    }
}
