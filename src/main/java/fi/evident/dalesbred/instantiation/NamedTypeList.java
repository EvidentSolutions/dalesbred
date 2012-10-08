package fi.evident.dalesbred.instantiation;

import java.util.Arrays;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

/**
 * Represents a named list of types, e.g. the result types of SQL-query.
 */
public final class NamedTypeList {

    private final String[] names;
    private final Class<?>[] types;

    private NamedTypeList(String[] names, Class<?>[] types) {
        this.names = names;
        this.types = types;
    }

    public int size() {
        return types.length;
    }

    public Class<?> getType(int index) {
        return types[index];
    }

    @Override
    public String toString() {
        return Arrays.toString(types);
    }

    public static Builder builder(int size) {
        return new Builder(size);
    }

    public static class Builder {
        private int index = 0;

        private final String[] names;
        private final Class<?>[] types;

        private Builder(int size) {
            this.names = new String[size];
            this.types = new Class<?>[size];
        }

        public void add(String name, Class<?> type) {
            this.names[index] = requireNonNull(name);
            this.types[index] = requireNonNull(type);
            index++;
        }

        public NamedTypeList build() {
            if (index != names.length)
                throw new IllegalStateException("expected " + names.length + " items, but got only " + index);

            index = -1; // set the index to invalid value so that we can no longer modify the class
            return new NamedTypeList(names, types);
        }
    }
}
