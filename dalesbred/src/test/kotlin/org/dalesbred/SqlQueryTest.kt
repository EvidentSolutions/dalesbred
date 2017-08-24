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

package org.dalesbred

import org.dalesbred.query.SqlQuery
import org.junit.Test
import java.lang.IllegalArgumentException
import java.sql.ResultSet
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.fail

class SqlQueryTest {

    @Test
    fun toStringProvidesMeaningfulInformation() {
        assertEquals("select bar from foo where id=? [42, null]",
                SqlQuery.query("select bar from foo where id=?", 42, null).toString())
    }

    @Test
    fun queriesHaveStructuralEquality() {
        assertEquals(SqlQuery.query("select * from foo"), SqlQuery.query("select * from foo"))
        assertEquals(SqlQuery.query("select * from foo", 1, 2), SqlQuery.query("select * from foo", 1, 2))

        assertNotEquals(SqlQuery.query("select * from foo"), SqlQuery.query("select * from bar"))
        assertNotEquals(SqlQuery.query("select * from foo", 1, 2), SqlQuery.query("select * from foo", 1, 3))
    }

    @Test
    fun hashCodeObeysEquality() {
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
    fun namedQueryFromMap() {
        val query = SqlQuery.namedQuery("select * from foo where name = :name", mapOf("name" to "bar"))

        assertEquals("select * from foo where name = ?", query.sql)
        assertEquals(listOf("bar"), query.arguments)
    }

    @Test
    fun namedQueryFromBean() {
        val query = SqlQuery.namedQuery("select * from foo where name = :name", ExampleNamed("bar"))

        assertEquals("select * from foo where name = ?", query.sql)
        assertEquals(listOf("bar"), query.arguments)
    }

    @Test
    fun illegalFetchSize() {
        val query = SqlQuery.query("select * from foo")
        assertFailsWith<IllegalArgumentException> {
            query.fetchSize = -1
        }
    }

    @Test
    fun illegalFetchDirection() {
        val query = SqlQuery.query("select * from foo")
        assertFailsWith<IllegalArgumentException> {
            query.fetchDirection = -1
        }
    }

    class ExampleNamed(val name: String)
}
