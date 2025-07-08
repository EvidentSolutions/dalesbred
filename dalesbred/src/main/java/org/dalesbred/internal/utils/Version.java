package org.dalesbred.internal.utils;

import org.jetbrains.annotations.NotNull;

import static java.lang.Math.min;

public final class Version implements Comparable<Version> {

    private final int[] numbers;

    // Some drivers (e.g. MySQL and H2) return version strings that do not look like typical SemVer strings and hence will not work with this
    private Version(@NotNull String version) throws IllegalArgumentException {
        String[] split = version.split("\\-")[0].split("\\.");
        numbers = new int[split.length];
        for (int i = 0; i < split.length; i++)
            numbers[i] = Integer.parseInt(split[i]);
    }

    public static Version parse(@NotNull String version) {
        return new Version(version);
    }

    @Override
    public int compareTo(@NotNull Version another) {
        for (int i = 0; i < min(numbers.length, another.numbers.length); i++) {
            int result = Integer.compare(numbers[i], another.numbers[i]);
            if (result != 0)
                return result;
        }
        return Integer.compare(numbers.length, another.numbers.length);
    }

}
