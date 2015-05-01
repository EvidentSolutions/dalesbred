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
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class NamedParameterSqlParserTest {

    @Test
    public void simpleQuery() {
        assertNamedParameters("SELECT * FROM foo WHERE col = :col", "SELECT * FROM foo WHERE col = ?",
                singletonList("col"));
    }

    @Test
    public void queryLiteralLikeNamedParameter() {
        assertNamedParameters("SELECT * FROM foobaz f WHERE f.id = :id AND f.col = ':notanamedparameter'",
                "SELECT * FROM foobaz f WHERE f.id = ? AND f.col = ':notanamedparameter'",
                singletonList("id"));
    }

    @Test
    public void queryWithLineCommentWithoutLineEnd() {
        assertNamedParameters("SELECT *, f.col::TEXT FROM foobar f WHERE f.id = :id -- comment :notnamedparameter",
                "SELECT *, f.col::TEXT FROM foobar f WHERE f.id = ? -- comment :notnamedparameter",
                singletonList("id"));
    }

    @Test
    public void queryWithLineComment() {
        assertNamedParameters("SELECT *, f.col::TEXT FROM foobar f -- comment :notnamedparameter \n WHERE f.id = :id",
                "SELECT *, f.col::TEXT FROM foobar f -- comment :notnamedparameter \n WHERE f.id = ?",
                singletonList("id"));
    }

    @Test
    public void queryWithBlockComment() {
        assertNamedParameters("SELECT *, f.col::TEXT /* comment :notanamedparameter */ FROM foobar f WHERE f.id = :id",
                "SELECT *, f.col::TEXT /* comment :notanamedparameter */ FROM foobar f WHERE f.id = ?",
                singletonList("id"));
    }

    @Test
    public void queryWithPostgresqlCast() {
        assertNamedParameters("SELECT *, f.col::TEXT FROM foobar f WHERE f.id = :id",
                "SELECT *, f.col::TEXT FROM foobar f WHERE f.id = ?",
                singletonList("id"));
    }

    @Test
    public void complexQuery() {
        assertNamedParameters("SELECT *, 1::TEXT FROM foobar f WHERE f.id = :id AND /* comment :notparameter */ f.qwerty = :bar*/snafu*/ AND f.literal = 'laalaa :pai puppa' AND f.test =:test /*what*/   /*  */ -- ef  ",
                "SELECT *, 1::TEXT FROM foobar f WHERE f.id = ? AND /* comment :notparameter */ f.qwerty = ?*/snafu*/ AND f.literal = 'laalaa :pai puppa' AND f.test =? /*what*/   /*  */ -- ef  ",
                asList("id", "bar", "test"));
    }

    @Test
    public void quotedStrings() {
        assertNamedParameters("select 'foo '' :bar'", "select 'foo '' :bar'", emptyList());
    }

    @Test
    public void doubleQuotes() {
        assertNamedParameters("select \" :bar  \"", "select \" :bar  \"", emptyList());
    }

    private static void assertNamedParameters(@SQL String sql, @SQL String expected, List<String> parameters) {
        NamedParameterSql result = NamedParameterSqlParser.parseSqlStatement(sql);

        assertThat(result.getSql(), is(expected));
        assertThat(result.getParameterNames(), is(parameters));
    }
}
