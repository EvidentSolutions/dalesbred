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

package org.dalesbred.kotlin

import org.dalesbred.Database
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DatabaseExtensionsTest {

    private val db = createInMemoryHSQLDatabase()

    @Test
    fun findAll() {
        assertEquals(listOf(Department(1, "foo"), Department(2, "bar")),
                db.findAll<Department>("select * from (values (1, 'foo'), (2, 'bar')) d"))
    }

    @Test
    fun findAll_customMapper() {
        val result = db.findAll("select * from (values (1, 'foo'), (2, 'bar')) d") { rs ->
            rs.getInt(1) to rs.getString(2).reversed()
        }

        assertEquals(listOf(1 to "oof", 2 to "rab"), result)
    }

    @Test
    fun findUnique() {
        assertEquals(Department(1, "foo"), db.findUnique<Department>("select * from (values (1, 'foo')) d"))
    }

    @Test
    fun findUnique_customMapper() {
        val result = db.findUnique("select * from (values (1, 'foo')) d") { rs ->
            rs.getInt(1) to rs.getString(2).reversed()
        }

        assertEquals(1 to "oof", result)
    }

    @Test
    fun findUniqueOrNull_existing() {
        assertEquals(Department(1, "foo"), db.findUniqueOrNull<Department>("select * from (values (1, 'foo')) d"))
    }

    @Test
    fun findUniqueOrNull_nonexistent() {
        assertNull(db.findUniqueOrNull<Department>("select * from (values (1, 'foo')) d where 1 = 2"))
    }

    @Test
    fun findUniqueOrNull_customMapper() {
        val result = db.findUniqueOrNull("select * from (values (1, 'foo')) d") { rs ->
            rs.getInt(1) to rs.getString(2).reversed()
        }

        assertEquals(1 to "oof", result)
    }

    @Test
    fun findOptional_existing() {
        assertEquals(Optional.of(Department(1, "foo")), db.findOptional<Department>("select * from (values (1, 'foo')) d"))
    }

    @Test
    fun findOptional_nonexistent() {
        assertEquals(Optional.empty(), db.findOptional<Department>("select * from (values (1, 'foo')) d where 1 = 2"))
    }

    @Test
    fun findOptional_customMapper() {
        val result = db.findOptional("select * from (values (1, 'foo')) d") { rs ->
            rs.getInt(1) to rs.getString(2).reversed()
        }

        assertEquals(Optional.of(1 to "oof"), result)
    }

    @Test
    fun findMap() {
        val map = db.findMap<Int,String>("select * from (values (1, 'foo'), (2, 'bar')) d")

        assertEquals(mapOf(1 to "foo", 2 to "bar"), map)
    }

    @Test
    fun executeQuery() {
        val result = db.executeQuery("select * from (values (1, 'foo'), (2, 'bar')) d") { rs ->
            val ints = mutableListOf<Int>()
            val strs = mutableListOf<String>()
            while (rs.next()) {
                ints += rs.getInt(1)
                strs += rs.getString(2)
            }

            ints to strs
        }

        assertEquals(listOf(1, 2) to listOf("foo", "bar"), result)
    }

    data class Department(val id: Int, val name: String)

    private fun createInMemoryHSQLDatabase(): Database =
            Database.forUrlAndCredentials("jdbc:hsqldb:mem:test;hsqldb.tx=mvcc", "sa", "")
}
