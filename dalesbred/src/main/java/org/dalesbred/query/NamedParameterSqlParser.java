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

package org.dalesbred.query;

import org.dalesbred.annotation.SQL;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class NamedParameterSqlParser {

    private static final SkippableBlock[] SKIPPABLE_BLOCKS = { new SkippableBlock("'",  "'",  false),
                                                               new SkippableBlock("\"", "\"", false),
                                                               new SkippableBlock("/*", "*/", false),
                                                               new SkippableBlock("::", "", false),
                                                               new SkippableBlock("--", "\n", true) };

    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("\\w+");

    @NotNull
    private final Lexer lexer;
    private final StringBuilder sqlBuilder;
    private final List<String> parameterNames = new ArrayList<>();

    private NamedParameterSqlParser(@SQL @NotNull String sql) {
        this.lexer = new Lexer(sql);
        this.sqlBuilder = new StringBuilder(sql.length());
    }

    public static NamedParameterSql parseSqlStatement(@NotNull @SQL String sql) {
        return new NamedParameterSqlParser(sql).parse();
    }

    private NamedParameterSql parse() {
        while (lexer.hasMore())
            parseNext();

        return new NamedParameterSql(sqlBuilder.toString(), parameterNames);
    }

    private void parseNext() {
        SkippableBlock skippableBlock = findSkippableBlock();
        if (skippableBlock != null) {
            sqlBuilder.append(lexer.readBlock(skippableBlock));

        } else if (lexer.lookingAt(":")) {
            sqlBuilder.append('?');
            parameterNames.add(parseName());

        } else if (lexer.lookingAt("?")) {
            throw new SqlSyntaxException("SQL cannot contain traditional ? placeholders.", lexer.sql);

        } else {
            sqlBuilder.append(lexer.readChar());
        }
    }

    private String parseName() {
        lexer.expect(":");
        String name = lexer.readRegexp(IDENTIFIER_PATTERN);
        if (name != null)
            return name;
        else
            throw new SqlSyntaxException("SQL cannot end to named parameter without name", lexer.sql);
    }

    @Nullable
    private SkippableBlock findSkippableBlock() {
        for (SkippableBlock block : SKIPPABLE_BLOCKS)
            if (lexer.lookingAt(block.start))
                return block;

        return null;
    }

    private static class SkippableBlock {
        private final String start;
        private final String end;
        private final boolean blockEndsWhenStreamRunsOut;

        private SkippableBlock(String start, String end, boolean blockEndsWhenStreamRunsOut) {
            this.start = start;
            this.end = end;
            this.blockEndsWhenStreamRunsOut = blockEndsWhenStreamRunsOut;
        }
    }

    private static final class Lexer {
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

        @Nullable
        private String readRegexp(@NotNull Pattern pattern) {
            Matcher matcher = pattern.matcher(sql.substring(offset));
            if (matcher.lookingAt()) {
                String result = matcher.group(0);
                offset += result.length();
                return result;
            } else {
                return null;
            }
        }

        private String readBlock(SkippableBlock skippableBlock) {
            int startOffset = offset;

            expect(skippableBlock.start);

            int nextHit = findNext(skippableBlock.end);
            if (nextHit != -1) {
                offset = nextHit + skippableBlock.end.length();
            } else {
                if (skippableBlock.blockEndsWhenStreamRunsOut)
                    offset = sql.length();
                else
                    throw new SqlSyntaxException("Block end not found: \"" + skippableBlock.end + "\".", sql);
            }

            return sql.substring(startOffset, offset);
        }

        private int findNext(@NotNull String substring) {
            return sql.indexOf(substring, offset);
        }

        public char readChar() {
            return sql.charAt(offset++);
        }
    }
}
