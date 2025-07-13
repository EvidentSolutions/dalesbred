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
