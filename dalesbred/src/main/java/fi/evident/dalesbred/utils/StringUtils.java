package fi.evident.dalesbred.utils;

import org.jetbrains.annotations.NotNull;

import static java.lang.Character.isUpperCase;
import static java.lang.Character.toLowerCase;

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
