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

import org.dalesbred.SQL;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NamedParameterSqlParserTest {

    @Test
    public void simpleQuery() {
        @SQL String sql = "SELECT * FROM foo WHERE col = :col";
        @SQL String traditionalSql = "SELECT * FROM foo WHERE col = ?";

        assertNamedParameters(NamedParameterSqlParser.parseSqlStatement(sql), traditionalSql, "col");
    }

    @Test
    public void queryLiteralLikeNamedParameter() {
        @SQL String sql = "SELECT * FROM foobaz f WHERE f.id = :id AND f.col = ':notanamedparameter'";
        @SQL String traditionalSql = "SELECT * FROM foobaz f WHERE f.id = ? AND f.col = ':notanamedparameter'";

        assertNamedParameters(NamedParameterSqlParser.parseSqlStatement(sql), traditionalSql, "id");
    }

    @Test
    public void queryWithLineCommentWithoutLineEnd() {
        @SQL String sql = "SELECT *, f.col::TEXT FROM foobar f WHERE f.id = :id -- comment :notnamedparameter";
        @SQL String traditionalSql = "SELECT *, f.col::TEXT FROM foobar f WHERE f.id = ? -- comment :notnamedparameter";

        assertNamedParameters(NamedParameterSqlParser.parseSqlStatement(sql), traditionalSql, "id");
    }

    @Test
    public void queryWithLineComment() {
        @SQL String sql = "SELECT *, f.col::TEXT FROM foobar f -- comment :notnamedparameter \n WHERE f.id = :id";
        @SQL String traditionalSql = "SELECT *, f.col::TEXT FROM foobar f -- comment :notnamedparameter \n WHERE f.id = ?";

        assertNamedParameters(NamedParameterSqlParser.parseSqlStatement(sql), traditionalSql, "id");
    }

    @Test
    public void queryWithBlockComment() {
        @SQL String sql = "SELECT *, f.col::TEXT /* comment :notanamedparameter */ FROM foobar f WHERE f.id = :id";
        @SQL String traditionalSql = "SELECT *, f.col::TEXT /* comment :notanamedparameter */ FROM foobar f WHERE f.id = ?";

        assertNamedParameters(NamedParameterSqlParser.parseSqlStatement(sql), traditionalSql, "id");
    }

    @Test
    public void queryWithPostgresqlCast() {
        @SQL String sql = "SELECT *, f.col::TEXT FROM foobar f WHERE f.id = :id";
        @SQL String traditionalSql = "SELECT *, f.col::TEXT FROM foobar f WHERE f.id = ?";

        assertNamedParameters(NamedParameterSqlParser.parseSqlStatement(sql), traditionalSql, "id");
    }

    @Test
    public void complexQuery() {
        @SQL String sql = "SELECT *, 1::TEXT FROM foobar f WHERE f.id = :id AND /* comment :notparameter */ f.qwerty = :bar*/snafu*/ AND f.literal = 'laalaa :pai puppa' AND f.test =:test /*what*/   /*  */ -- ef  ";
        @SQL String traditionalSql = "SELECT *, 1::TEXT FROM foobar f WHERE f.id = ? AND /* comment :notparameter */ f.qwerty = ?*/snafu*/ AND f.literal = 'laalaa :pai puppa' AND f.test =? /*what*/   /*  */ -- ef  ";

        assertNamedParameters(NamedParameterSqlParser.parseSqlStatement(sql), traditionalSql, "id", "bar", "test");
    }

    @Test
    public void quotedStrings() {
        @SQL String sql = "select 'foo '' :bar'";

        assertNamedParameters(NamedParameterSqlParser.parseSqlStatement(sql), sql);
    }

    @Test
    public void doubleQuotes() {
        @SQL String sql = "select \" :bar  \"";

        assertNamedParameters(NamedParameterSqlParser.parseSqlStatement(sql), sql);
    }

    private static void assertNamedParameters(NamedParameterSql namedParameterSql, String traditionalSql, Object... parameters) {
        assertEquals(traditionalSql, namedParameterSql.getSql());
        assertEquals(parameters.length, namedParameterSql.getParameterNames().size());
        for (int i = 0; i < parameters.length; i++) {
            assertEquals(parameters[i], namedParameterSql.getParameterNames().get(i));
        }
    }
}
