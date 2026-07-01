package org.dalesbred.internal.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;

public final class TypeUtils {

    private TypeUtils() { }

    public static @NotNull Class<?> rawType(@NotNull Type type) {
        return switch (type) {
            case Class<?> aClass -> aClass;
            case ParameterizedType parameterizedType -> rawType(parameterizedType.getRawType());
            case TypeVariable<?> ignored ->
                // We could return one of the bounds, but it won't work if there are multiple bounds.
                // Therefore, just return object.
                Object.class;
            case WildcardType wildcardType -> rawType(wildcardType.getUpperBounds()[0]);
            case GenericArrayType genericArrayType -> arrayType(rawType(genericArrayType.getGenericComponentType()));
            default -> throw new IllegalArgumentException("unexpected type: " + type);
        };
    }

    public static @NotNull Type typeParameter(@NotNull Type type) {
        if (type instanceof ParameterizedType)
            return ((ParameterizedType) type).getActualTypeArguments()[0];

        return Object.class;
    }

    public static boolean isEnum(@NotNull Type type) {
        return (type instanceof Class<?>) && ((Class<?>) type).isEnum();
    }

    public static boolean isPrimitive(@NotNull Type type) {
        return (type instanceof Class<?>) && ((Class<?>) type).isPrimitive();
    }

    public static @NotNull Class<?> arrayType(@NotNull Class<?> type) {
        return Array.newInstance(type, 0).getClass();
    }

    public static @Nullable Type genericSuperClass(@NotNull Type type) {
        return rawType(type).getGenericSuperclass();
    }

    public static @NotNull Type[] genericInterfaces(@NotNull Type type) {
        return rawType(type).getGenericInterfaces();
    }

    public static boolean isAssignable(@NotNull Type target, @NotNull Type source) {
        // TODO: implement proper rules for generic types
        return isAssignableByBoxing(rawType(target), rawType(source));
    }

    private static boolean isAssignableByBoxing(@NotNull Class<?> target, @NotNull Class<?> source) {
        return Primitives.wrap(target).isAssignableFrom(Primitives.wrap(source));
    }
}
