package org.dalesbred.internal.instantiation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Factory for producing objects from given arguments.
 */
@FunctionalInterface
public interface Instantiator<T> {
    @Nullable
    T instantiate(@NotNull InstantiatorArguments arguments);
}
