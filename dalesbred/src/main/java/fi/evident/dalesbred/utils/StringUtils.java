/*
 * Copyright (c) 2012 Evident Solutions Oy
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

package fi.evident.dalesbred.utils;

import org.jetbrains.annotations.NotNull;

import static java.lang.Character.isUpperCase;
import static java.lang.Character.toLowerCase;

/**
 * Utilities for strings.
 */
public final class StringUtils {

    private StringUtils() { }

    /**
     * Converts words <em>CamelCasedWords</em> to <em>underscore_separated_words.</em>
     */
    @NotNull
    public static String upperCamelToLowerUnderscore(@NotNull String s) {
        StringBuilder sb = new StringBuilder(s.length() + 5);

        boolean candidateWordEnd = false;
        for (int i = 0, len = s.length(); i < len; i++) {
            char ch = s.charAt(i);

            if (isUpperCase(ch) && candidateWordEnd) {
                sb.append('_');
                candidateWordEnd = false;

            } else if (!isUpperCase(ch) && ch != '_') {
                candidateWordEnd = true;
            }

            sb.append(toLowerCase(ch));
        }

        return sb.toString();
    }
}
