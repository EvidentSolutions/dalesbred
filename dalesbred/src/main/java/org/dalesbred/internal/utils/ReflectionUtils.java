package org.dalesbred.internal.utils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import static java.lang.reflect.Modifier.isPublic;

public final class ReflectionUtils {

    private ReflectionUtils() {
    }

    public static @NotNull Optional<Field> findField(@NotNull Class<?> cl, @NotNull String name) {
        try {
            return Optional.of(cl.getField(name));
        } catch (NoSuchFieldException e) {
            return Optional.empty();
        }
    }

    public static @NotNull Optional<Method> findGetter(@NotNull Class<?> cl, @NotNull String propertyName) {
        String capitalizedName = StringUtils.capitalize(propertyName);

        try {
            return Optional.of(cl.getMethod("get" + capitalizedName));
        } catch (NoSuchMethodException e) {
            try {
                return Optional.of(cl.getMethod("is" + capitalizedName));
            } catch (NoSuchMethodException e1) {
                return Optional.empty();
            }
        }
    }

    public static void makeAccessible(@NotNull Executable obj) throws SecurityException {
        if (!isPublic(obj.getModifiers()))
            obj.setAccessible(true);
    }
}
