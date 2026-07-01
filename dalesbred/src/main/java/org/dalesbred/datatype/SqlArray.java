package org.dalesbred.datatype;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * Wrapper for values that are to be bound as {@link java.sql.Array} objects
 * when executing queries.
 */
public final class SqlArray {

    /** Database specific type name of the array */
    private final @NotNull String type;

    /** Values for the array */
    private final @NotNull List<?> values;

    private SqlArray(@NotNull String type, @NotNull Collection<?> values) {
        this.type = requireNonNull(type);
        this.values = List.copyOf(values);
    }

    /**
     * Constructs array of specified type.
     *
     * @param type database type for the array
     * @param values for the array
     */
    public static @NotNull SqlArray of(@NotNull String type, @NotNull Collection<?> values) {
        return new SqlArray(type, values);
    }

    /**
     * Constructs array of specified type.
     *
     * @param type database type for the array
     * @param values for the array
     */
    public static @NotNull SqlArray of(@NotNull String type, @NotNull Object[] values) {
        return of(type, asList(values));
    }

    /**
     * Constructs varchar array of given values.
     */
    public static @NotNull SqlArray varchars(@NotNull Collection<String> values) {
        return of("varchar", values);
    }

    /**
     * Constructs varchar array of given values.
     */
    public static @NotNull SqlArray varchars(@NotNull String... values) {
        return varchars(asList(values));
    }

    /**
     * Returns the database type for the array.
     */
    public @NotNull String getType() {
        return type;
    }

    /**
     * Returns the values of the array.
     */
    public @NotNull List<?> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "SQLArray[type=" + type + ", values=" + values + ']';
    }
}
