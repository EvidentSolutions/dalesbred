/*
 * Copyright (c) 2017 Evident Solutions Oy
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

package org.dalesbred.query;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

final class NamedParameterSqlParser {

    /** The various patterns we need to skip combined to single regex so that it will be executed at once */
    private static final Pattern SKIP_PATTERN = Pattern.compile("('[^']*'|\"[^\"]*\"|::|--[^\n]*)");

    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("\\w+");

    private final @NotNull Lexer lexer;
    private final StringBuilder sqlBuilder;
    private final List<String> parameterNames = new ArrayList<>();

    private NamedParameterSqlParser(@Language("SQL") @NotNull String sql) {
        this.lexer = new Lexer(sql);
        this.sqlBuilder = new StringBuilder(sql.length());
    }

    public static @NotNull NamedParameterSql parseSqlStatement(@NotNull @Language("SQL") String sql) {
        NamedParameterSqlParser parser = new NamedParameterSqlParser(requireNonNull(sql));

        while (parser.lexer.hasMore())
            parser.parseNext();

        return new NamedParameterSql(parser.sqlBuilder.toString(), parser.parameterNames);
    }

    private void parseNext() {
        CharSequence skipped = lexer.readRegexp(SKIP_PATTERN);
        if (skipped != null) {
            sqlBuilder.append(skipped);

        } else if (lexer.lookingAt("/*")) {
            sqlBuilder.append(readUntil("*/"));

        } else if (lexer.lookingAt(":")) {
            sqlBuilder.append('?');
            parameterNames.add(parseName());

        } else if (lexer.lookingAt("?")) {
            throw new SqlSyntaxException("SQL cannot contain traditional ? placeholders.", lexer.sql);

        } else {
            sqlBuilder.append(lexer.readChar());
        }
    }

    private @NotNull String parseName() {
        lexer.expect(":");
        CharSequence name = lexer.readRegexp(IDENTIFIER_PATTERN);
        if (name != null)
            return name.toString();
        else
            throw new SqlSyntaxException("SQL cannot end to named parameter without name", lexer.sql);
    }

    public @NotNull String readUntil(@NotNull String end) {
        int startOffset = lexer.offset;

        int nextHit = lexer.findNext(end);
        if (nextHit != -1)
            lexer.offset = nextHit + end.length();
        else
            throw new SqlSyntaxException("Block end not found: \"" + end + "\".", lexer.sql);

        return lexer.sql.substring(startOffset, lexer.offset);
    }

    private static final class Lexer implements CharSequence {
        private final String sql;
        private int offset;

        public Lexer(String sql) {
            this.sql = sql;
        }

        private boolean hasMore() {
            return offset < sql.length();
        }

        private boolean lookingAt(@NotNull String prefix) {
            return sql.startsWith(prefix, offset);
        }

        private void expect(@NotNull String prefix) {
            if (lookingAt(prefix))
                offset += prefix.length();
            else
                throw new SqlSyntaxException("expected '" + prefix + '\'', sql);
        }

        private @Nullable CharSequence readRegexp(@NotNull Pattern pattern) {
            Matcher matcher = pattern.matcher(this);
            if (matcher.lookingAt()) {
                CharSequence result = subSequence(0, matcher.end());
                offset += result.length();
                return result;
            } else {
                return null;
            }
        }

        @Override
        public int length() {
            return sql.length() - offset;
        }

        @Override
        public char charAt(int index) {
            return sql.charAt(offset + index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return sql.substring(offset + start, offset + end);
        }

        @Override
        public String toString() {
            return sql.substring(offset);
        }

        private int findNext(@NotNull String substring) {
            return sql.indexOf(substring, offset);
        }

        public char readChar() {
            return sql.charAt(offset++);
        }
    }
}
