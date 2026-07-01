package org.dalesbred

import org.dalesbred.query.SqlQuery
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class SqlQueryTest {

    @Test
    fun `toString provides meaningful information`() {
        assertEquals("select bar from foo where id=? [42, null]",
                SqlQuery.query("select bar from foo where id=?", 42, null).toString())
    }

    @Test
    fun `queries have structural equality`() {
        assertEquals(SqlQuery.query("select * from foo"), SqlQuery.query("select * from foo"))
        assertEquals(SqlQuery.query("select * from foo", 1, 2), SqlQuery.query("select * from foo", 1, 2))

        assertNotEquals(SqlQuery.query("select * from foo"), SqlQuery.query("select * from bar"))
        assertNotEquals(SqlQuery.query("select * from foo", 1, 2), SqlQuery.query("select * from foo", 1, 3))
    }

    @Test
    fun `hashCode obeys equality`() {
        assertEquals(SqlQuery.query("select * from foo").hashCode(), SqlQuery.query("select * from foo").hashCode())
        assertEquals(SqlQuery.query("select * from foo", 1, 2).hashCode(), SqlQuery.query("select * from foo", 1, 2).hashCode())
    }

    @Test
    fun accessors() {
        val query = SqlQuery.query("select * from foo", listOf(1, 2, 3))

        assertEquals("select * from foo", query.sql)
        assertEquals(listOf(1, 2, 3), query.arguments)
    }

    @Test
    fun `named query from map`() {
        val query = SqlQuery.namedQuery("select * from foo where name = :name", mapOf("name" to "bar"))

        assertEquals("select * from foo where name = ?", query.sql)
        assertEquals(listOf("bar"), query.arguments)
    }

    @Test
    fun `named query from bean`() {
        val query = SqlQuery.namedQuery("select * from foo where name = :name", ExampleNamed("bar"))

        assertEquals("select * from foo where name = ?", query.sql)
        assertEquals(listOf("bar"), query.arguments)
    }

    @Test
    fun `illegal fetch size`() {
        val query = SqlQuery.query("select * from foo")
        assertFailsWith<IllegalArgumentException> {
            query.fetchSize = -1
        }
    }

    class ExampleNamed(val name: String)
}
