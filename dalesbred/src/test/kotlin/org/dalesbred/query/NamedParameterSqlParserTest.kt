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

package org.dalesbred.query

import org.intellij.lang.annotations.Language
import kotlin.test.Test
import kotlin.test.assertEquals

class NamedParameterSqlParserTest {

    @Test
    fun simpleQuery() {
        assertNamedParameters("SELECT * FROM foo WHERE col = :col", "SELECT * FROM foo WHERE col = ?",
                listOf("col"))
    }

    @Test
    fun queryLiteralLikeNamedParameter() {
        assertNamedParameters("SELECT * FROM foobaz f WHERE f.id = :id AND f.col = ':notanamedparameter'",
                "SELECT * FROM foobaz f WHERE f.id = ? AND f.col = ':notanamedparameter'",
                listOf("id"))
    }

    @Test
    fun queryWithLineCommentWithoutLineEnd() {
        assertNamedParameters("SELECT *, f.col::TEXT FROM foobar f WHERE f.id = :id -- comment :notnamedparameter",
                "SELECT *, f.col::TEXT FROM foobar f WHERE f.id = ? -- comment :notnamedparameter",
                listOf("id"))
    }

    @Test
    fun queryWithLineComment() {
        assertNamedParameters("SELECT *, f.col::TEXT FROM foobar f -- comment :notnamedparameter \n WHERE f.id = :id",
                "SELECT *, f.col::TEXT FROM foobar f -- comment :notnamedparameter \n WHERE f.id = ?",
                listOf("id"))
    }

    @Test
    fun queryWithBlockComment() {
        assertNamedParameters("SELECT *, f.col::TEXT /* comment :notanamedparameter */ FROM foobar f WHERE f.id = :id",
                "SELECT *, f.col::TEXT /* comment :notanamedparameter */ FROM foobar f WHERE f.id = ?",
                listOf("id"))
    }

    @Test
    fun queryWithPostgresqlCast() {
        assertNamedParameters("SELECT *, f.col::TEXT FROM foobar f WHERE f.id = :id",
                "SELECT *, f.col::TEXT FROM foobar f WHERE f.id = ?",
                listOf("id"))
    }

    @Test
    fun complexQuery() {
        assertNamedParameters("SELECT *, 1::TEXT FROM foobar f WHERE f.id = :id AND /* comment :notparameter */ f.qwerty = :bar*/snafu*/ AND f.literal = 'laalaa :pai puppa' AND f.test =:test /*what*/   /*  */ -- ef  ",
                "SELECT *, 1::TEXT FROM foobar f WHERE f.id = ? AND /* comment :notparameter */ f.qwerty = ?*/snafu*/ AND f.literal = 'laalaa :pai puppa' AND f.test =? /*what*/   /*  */ -- ef  ",
                listOf("id", "bar", "test"))
    }

    @Test
    fun quotedStrings() {
        assertNamedParameters("select 'foo '' :bar'", "select 'foo '' :bar'", emptyList<String>())
    }

    @Test
    fun doubleQuotes() {
        assertNamedParameters("select \" :bar  \"", "select \" :bar  \"", emptyList<String>())
    }

    private fun assertNamedParameters(@Language("SQL") sql: String,
                                      @Language("SQL") expected: String,
                                      parameters: List<String>) {
        val result = NamedParameterSqlParser.parseSqlStatement(sql)

        assertEquals(expected, result.sql)
        assertEquals(parameters, result.parameterNames)
    }
}
