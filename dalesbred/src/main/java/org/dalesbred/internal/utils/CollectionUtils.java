package org.dalesbred.internal.utils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

import static java.util.stream.Collectors.toCollection;

public final class CollectionUtils {

    private CollectionUtils() { }

    public static @NotNull <A,B> List<B> mapToList(@NotNull Collection<? extends A> xs, @NotNull Function<? super A, ? extends B> mapper) {
        return xs.stream().map(mapper).collect(toListWithCapacity(xs.size()));
    }

    public static @NotNull Object arrayOfType(@NotNull Class<?> elementType, @NotNull List<?> values) {
        int length = values.size();

        Object result = Array.newInstance(elementType, length);
        for (int i = 0; i < length; i++)
            Array.set(result, i, values.get(i));

        return result;
    }

    private static @NotNull <T> Collector<T, ?, ArrayList<T>> toListWithCapacity(int capacity) {
        return toCollection(() -> new ArrayList<>(capacity));
    }
}
