package fi.evident.dalesbred.utils;

import org.jetbrains.annotations.NotNull;

public final class Primitives {

    Primitives() { }

    /**
     * Returns the corresponding wrapper type for a primitive type, or the type itself if it is not a primitive type.
     */
    @NotNull
    public static Class<?> wrap(@NotNull Class<?> type) {
        return (type == boolean.class) ? Boolean.class
             : (type == byte.class)    ? Byte.class
             : (type == char.class)    ? Character.class
             : (type == short.class)   ? Short.class
             : (type == int.class)     ? Integer.class
             : (type == long.class)    ? Long.class
             : (type == float.class)   ? Float.class
             : (type == double.class)  ? Double.class
             : type;
    }

    /**
     * Returns the corresponding primitive type for a wrapper type, or the type itself if it is not a wrapper.
     */
    @NotNull
    public static Class<?> unwrap(@NotNull Class<?> type) {
        return (type == Boolean.class)   ? boolean.class
             : (type == Byte.class)      ? byte.class
             : (type == Character.class) ? char.class
             : (type == Short.class)     ? short.class
             : (type == Integer.class)   ? int.class
             : (type == Long.class)      ? long.class
             : (type == Float.class)     ? float.class
             : (type == Double.class)    ? double.class
             : type;
    }
}
