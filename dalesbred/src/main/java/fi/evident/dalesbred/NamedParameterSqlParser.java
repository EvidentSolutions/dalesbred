/*
 * Copyright (c) 2013 Evident Solutions Oy
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
package fi.evident.dalesbred;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

final class NamedParameterSqlParser {

    private static final SkippableBlock[] SKIPPABLE_BLOCKS = { new SkippableBlock("'",  "'",  false),
                                                               new SkippableBlock("\"", "\"", false),
                                                               new SkippableBlock("/*", "*/", false),
                                                               new SkippableBlock("--", "\n", true) };

    public static NamedParameterSql parseSqlStatement(@NotNull @SQL String sql) throws IllegalArgumentException {

        StringBuilder traditionalSqlBuilder = new StringBuilder(sql.length());
        List<String> namedParameters = new ArrayList<String>();

        int offset = 0;
        while (isNotAtEnd(sql, offset)) {
            int skippableOffset = skipSkippableSequences(sql, offset);
            if (skippableOffset != offset) {
                traditionalSqlBuilder.append(sql.substring(offset, skippableOffset));
                offset = skippableOffset;
            } else {
                traditionalSqlBuilder.append('?');
                int namedParameterOffset = skipNamedParameter(sql, offset);
                namedParameters.add(sql.substring(offset+1, namedParameterOffset));
                offset = namedParameterOffset;
            }
        }
        return new NamedParameterSql(sql, traditionalSqlBuilder.toString(), namedParameters);
    }

    private static int skipNamedParameter(@NotNull @SQL String sql, int offset) {
        offset++; // skip leading ':'

        while(isNotAtEnd(sql, offset) && Character.isLetter(sql.charAt(offset)))
            offset++;

        return offset;
    }

    private static int skipSkippableSequences(@NotNull @SQL String sql, int offset) {
        while (isNotAtEnd(sql, offset)) {
            char c = sql.charAt(offset);
            if (c == ':')
                if (isNotAtEnd(sql, offset+1))
                    if (sql.charAt(offset+1) == ':')
                        offset += 2; // skip postgresql cast special case
                    else
                        return offset; // actual named parameter start
                else
                    throw new IllegalArgumentException("SQL cannot end to named parameter without name");
            else if (c == '?')
                throw new IllegalArgumentException("SQL cannot contain traditional ? placeholders. [" + sql + ']');
            else
                offset = skipCharacterOrBlock(sql, offset);
        }
        return sql.length();
    }

    private static int skipCharacterOrBlock(@SQL @NotNull String sql, int offset) {
        SkippableBlock skippableBlock = findStartingSkippableBlock(sql, offset);
        if (skippableBlock != null)
            return skipToBlockEnd(sql, offset, skippableBlock);
        else
            return offset+1;
    }

    @Nullable
    private static SkippableBlock findStartingSkippableBlock(@NotNull @SQL String sql, int offset) {
        for (SkippableBlock block : SKIPPABLE_BLOCKS)
            if (sql.startsWith(block.start, offset))
                return block;

        return null;
    }

    private static int skipToBlockEnd(@NotNull @SQL String sql, int offset, SkippableBlock skippableBlock) {
        offset += skippableBlock.start.length();

        while(isNotAtEnd(sql, offset) && !sql.startsWith(skippableBlock.end, offset))
            offset++;

        if (isNotAtEnd(sql, offset))
            return offset + skippableBlock.end.length();
        else
            if (skippableBlock.blockEndsWhenStreamRunsOut)
                return sql.length();
            else
                throw new IllegalArgumentException("Block end not found: \"" + skippableBlock.end + "\". [" + sql + ']');
    }

    private static boolean isNotAtEnd(String sql, int index) {
        return index < sql.length();
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

    private NamedParameterSqlParser() {}
}