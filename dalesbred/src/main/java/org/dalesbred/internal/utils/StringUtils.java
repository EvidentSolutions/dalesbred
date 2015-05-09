/*
 * Copyright (c) 2015 Evident Solutions Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
    @NotNull
    public static String capitalize(@NotNull String s) {
        return s.isEmpty() ? s : (toUpperCase(s.charAt(0)) + s.substring(1));
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
