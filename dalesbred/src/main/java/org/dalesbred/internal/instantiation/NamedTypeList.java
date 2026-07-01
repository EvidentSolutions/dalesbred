package org.dalesbred.internal.instantiation;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

/**
 * Represents a named list of types, e.g. the result types of SQL-query.
 */
public final class NamedTypeList {

    private final @NotNull List<String> names;

    private final @NotNull List<Type> types;

    private NamedTypeList(@NotNull List<String> names, @NotNull List<Type> types) {
        assert names.size() == types.size();
        this.names = unmodifiableList(names);
        this.types = types;
    }

    public int size() {
        return types.size();
    }

    public @NotNull String getName(int index) {
        return names.get(index);
    }

    public @NotNull Type getType(int index) {
        return types.get(index);
    }

    public @NotNull List<String> getNames() {
        return names;
    }

    public @NotNull NamedTypeList subList(int fromIndex, int toIndex) {
        return new NamedTypeList(names.subList(fromIndex, toIndex), types.subList(fromIndex, toIndex));
    }

    @Override
    public @NotNull String toString() {
        int size = types.size();

        @SuppressWarnings("MagicNumber")
        StringBuilder sb = new StringBuilder(10 + size * 30);

        sb.append('[');

        for (int i = 0; i < size; i++) {
            if (i != 0) sb.append(", ");

            sb.append(names.get(i)).append(": ").append(types.get(i).getTypeName());
        }

        sb.append(']');

        return sb.toString();
    }

    public static @NotNull Builder builder(int size) {
        return new Builder(size);
    }

    /**
     * Builder for {@link NamedTypeList}s.
     */
    public static class Builder {

        private boolean built = false;

        private final @NotNull List<String> names;

        private final @NotNull List<Type> types;

        private Builder(int size) {
            this.names = new ArrayList<>(size);
            this.types = new ArrayList<>(size);
        }

        public Builder add(@NotNull String name, @NotNull Type type) {
            if (built) throw new IllegalStateException("can't add items to builder that has been built");

            names.add(requireNonNull(name));
            types.add(requireNonNull(type));
            return this;
        }

        public @NotNull NamedTypeList build() {
            built = true;
            return new NamedTypeList(names, types);
        }
    }
}
