package org.dalesbred.query

import org.dalesbred.query.SqlQuery.query
import kotlin.test.*

class QueryBuilderTest {

    @Test
    fun `building simple query`() {
        val qb = QueryBuilder("select 42")

        assertEquals(query("select 42"), qb.build())
    }

    @Test
    fun `building simple query with arguments`() {
        val qb = QueryBuilder("select ?, ?", 42, "foo")

        assertEquals(query("select ?, ?", 42, "foo"), qb.build())
    }

    @Test
    fun `building query dynamically`() {
        val qb = QueryBuilder("select * from document")
        qb.append(" where id=?", 4).append(" or status=?", "rejected")

        assertEquals(query("select * from document where id=? or status=?", 4, "rejected"), qb.build())
    }

    @Test
    fun placeholders() {
        val qb = QueryBuilder("select * from document")
        val query = qb.append(" where id in (").appendPlaceholders(4).append(")").addArguments(1, 2, 3).addArgument(4).build()

        assertEquals(query("select * from document where id in (?,?,?,?)", 1, 2, 3, 4), query)
    }

    @Test
    fun empty() {
        assertTrue(QueryBuilder().isEmpty)
        assertTrue(QueryBuilder().append("").isEmpty)
        assertFalse(QueryBuilder("foo").isEmpty)
        assertFalse(QueryBuilder().append("foo").isEmpty)
    }

    @Test
    fun arguments() {
        assertFalse(QueryBuilder().hasArguments())
        assertTrue(QueryBuilder().addArgument(null).hasArguments())
    }

    @Test
    fun `placeholders for collection`() {
        val qb = QueryBuilder("select * from document")
        val query = qb.append(" where id in (").appendPlaceholders(listOf(1, 2, 3)).append(")").build()

        assertEquals(query("select * from document where id in (?,?,?)", 1, 2, 3), query)
    }

    @Test
    fun `zero placeholders`() {
        assertFailsWith<IllegalArgumentException> {
            QueryBuilder("select * from foo where id in (").appendPlaceholders(0).append(")")
        }
    }

    @Test
    fun `building empty query throws exception`() {
        assertFailsWith<IllegalStateException> {
            QueryBuilder().build()
        }
    }

    @Test
    fun `building empty query throws exception2`() {
        assertFailsWith<IllegalStateException> {
            QueryBuilder("").build()
        }
    }

    @Test
    fun placeholder() {
        assertEquals(QueryBuilder.PLACEHOLDER, QueryBuilder().appendPlaceholders(1).build().sql)
    }

    @Test
    fun `append query`() {
        val query = query("select * from foo where bar = ? and baz = ?", 42, "foobar")

        val qb = QueryBuilder()
        qb.append("select * from (").append(query).append(") where row < ?", 10)

        assertEquals(query("select * from (select * from foo where bar = ? and baz = ?) where row < ?", 42, "foobar", 10), qb.build())
    }
}
