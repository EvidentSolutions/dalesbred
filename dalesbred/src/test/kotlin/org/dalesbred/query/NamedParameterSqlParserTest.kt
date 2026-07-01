package org.dalesbred.query

import org.intellij.lang.annotations.Language
import kotlin.test.Test
import kotlin.test.assertEquals

class NamedParameterSqlParserTest {

    @Test
    fun `simple query`() {
        assertNamedParameters("SELECT * FROM foo WHERE col = :col", "SELECT * FROM foo WHERE col = ?",
                listOf("col"))
    }

    @Test
    fun `query literal like named parameter`() {
        assertNamedParameters("SELECT * FROM foobaz f WHERE f.id = :id AND f.col = ':notanamedparameter'",
                "SELECT * FROM foobaz f WHERE f.id = ? AND f.col = ':notanamedparameter'",
                listOf("id"))
    }

    @Test
    fun `query with line comment without line end`() {
        assertNamedParameters("SELECT *, f.col::TEXT FROM foobar f WHERE f.id = :id -- comment :notnamedparameter",
                "SELECT *, f.col::TEXT FROM foobar f WHERE f.id = ? -- comment :notnamedparameter",
                listOf("id"))
    }

    @Test
    fun `query with line comment`() {
        assertNamedParameters("SELECT *, f.col::TEXT FROM foobar f -- comment :notnamedparameter \n WHERE f.id = :id",
                "SELECT *, f.col::TEXT FROM foobar f -- comment :notnamedparameter \n WHERE f.id = ?",
                listOf("id"))
    }

    @Test
    fun `query with block comment`() {
        assertNamedParameters("SELECT *, f.col::TEXT /* comment :notanamedparameter */ FROM foobar f WHERE f.id = :id",
                "SELECT *, f.col::TEXT /* comment :notanamedparameter */ FROM foobar f WHERE f.id = ?",
                listOf("id"))
    }

    @Test
    fun `query with postgresql cast`() {
        assertNamedParameters("SELECT *, f.col::TEXT FROM foobar f WHERE f.id = :id",
                "SELECT *, f.col::TEXT FROM foobar f WHERE f.id = ?",
                listOf("id"))
    }

    @Test
    fun `complex query`() {
        assertNamedParameters("SELECT *, 1::TEXT FROM foobar f WHERE f.id = :id AND /* comment :notparameter */ f.qwerty = :bar*/snafu*/ AND f.literal = 'laalaa :pai puppa' AND f.test =:test /*what*/   /*  */ -- ef  ",
                "SELECT *, 1::TEXT FROM foobar f WHERE f.id = ? AND /* comment :notparameter */ f.qwerty = ?*/snafu*/ AND f.literal = 'laalaa :pai puppa' AND f.test =? /*what*/   /*  */ -- ef  ",
                listOf("id", "bar", "test"))
    }

    @Test
    fun `quoted strings`() {
        assertNamedParameters("select 'foo '' :bar'", "select 'foo '' :bar'", emptyList())
    }

    @Test
    fun `double quotes`() {
        assertNamedParameters("select \" :bar  \"", "select \" :bar  \"", emptyList())
    }

    private fun assertNamedParameters(@Language("SQL") sql: String,
                                      @Language("SQL") expected: String,
                                      parameters: List<String>) {
        val result = NamedParameterSqlParser.parseSqlStatement(sql)

        assertEquals(expected, result.sql())
        assertEquals(parameters, result.parameterNames())
    }
}
