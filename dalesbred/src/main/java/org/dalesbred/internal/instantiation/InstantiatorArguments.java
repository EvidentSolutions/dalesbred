package org.dalesbred.internal.instantiation;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * Contains the arguments of instantiator as well their names and types.
 */
public final class InstantiatorArguments {

    private final @NotNull NamedTypeList types;

    private final @NotNull List<?> values;

    public InstantiatorArguments(@NotNull NamedTypeList types, @NotNull Object[] values) {
        this(types, Arrays.asList(values));
    }

    public InstantiatorArguments(@NotNull NamedTypeList types, @NotNull List<?> values) {
        if (types.size() != values.size())
            throw new IllegalArgumentException("got " + types.size() + " types, but " + values.size() + " values");

        this.types = types;
        this.values = unmodifiableList(values);
    }

    public @NotNull NamedTypeList getTypes() {
        return types;
    }

    public @NotNull List<?> getValues() {
        return values;
    }

    public int size() {
        return types.size();
    }

    public Object getSingleValue() {
        if (values.size() != 1)
            throw new IllegalStateException("expected single argument, but got " + values.size());

        return values.getFirst();
    }
}
