package org.dalesbred.internal.utils;

import org.jetbrains.annotations.NotNull;

import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;

/**
 * Utilities for strings.
 */
public final class StringUtils {

    private StringUtils() { }

    /**
     * Returns given string with its first letter in uppercase.
     */
    public static @NotNull String capitalize(@NotNull String s) {
        return s.isEmpty() ? s : (toUpperCase(s.charAt(0)) + s.substring(1));
    }

    public static @NotNull String rightPad(@NotNull String s, int length, char padding) {
        if (s.length() >= length) return s;

        StringBuilder sb = new StringBuilder(length);
        sb.append(s);

        for (int i = length - s.length(); i > 0; i--)
            sb.append(padding);

        return sb.toString();
    }

    public static @NotNull String truncate(@NotNull String s, int length) {
        return truncate(s, length, "...");
    }

    public static @NotNull String truncate(@NotNull String s, int length, @NotNull String suffix) {
        if (s.length() <= length)
            return s;

        if (suffix.length() > length)
            return suffix.substring(0, length);

        return s.substring(0, length - suffix.length()) + suffix;
    }

    /**
     * Returns true if two strings are equal, apart from case differences and underscores.
     * Underscores in both sides are totally ignored.
     */
    public static boolean isEqualIgnoringCaseAndUnderscores(@NotNull String s1, @NotNull String s2) {
        int index1 = 0;
        int index2 = 0;
        int length1 = s1.length();
        int length2 = s2.length();

        while (index1 < length1 && index2 < length2) {
            char nameChar = s1.charAt(index1++);
            if (nameChar == '_') continue;

            char memberNameChar = s2.charAt(index2++);
            if (memberNameChar == '_') {
                index1--;
                continue;
            }

            if (toLowerCase(nameChar) != toLowerCase(memberNameChar))
                return false;
        }

        // Skip trailing underscores
        while (index1 < length1 && s1.charAt(index1) == '_') index1++;
        while (index2 < length2 && s2.charAt(index2) == '_') index2++;

        return index1 == length1 && index2 == length2;
    }
}
