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

import org.dalesbred.dialect.HsqldbDialect
import org.dalesbred.result.*
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.util.*
import kotlin.test.*

class DatabaseTest {

    private val db = TestDatabaseProvider.createInMemoryHSQLDatabase()

    @get:Rule val rule = TransactionalTestsRule(db)

    @Test
    fun meaningfulToString() {
        db.isAllowImplicitTransactions = true
        assertEquals("Database [dialect=${HsqldbDialect()}, allowImplicitTransactions=true]", db.toString())
    }

    @Test
    fun primitivesQueries() {
        assertEquals(42, db.findUniqueInt("values (42)"))
        assertEquals(42, db.findUnique(Int::class.java, "values (42)"))
        assertEquals(42L, db.findUnique(Long::class.java, "values (cast(42 as bigint))"))
        assertEquals(42.0f, db.findUnique(Float::class.java, "values (42.0)"))
        assertEquals(42.0, db.findUnique(Double::class.java, "values (42.0)"))
        assertEquals("foo", db.findUnique(String::class.java, "values ('foo')"))
        assertEquals(true, db.findUnique(Boolean::class.java, "values (true)"))
        assertNull(db.findUnique(Boolean::class.javaObjectType, "values (cast(null as boolean))"))
    }

    @Test
    fun bigNumbers() {
        assertEquals(BigDecimal("4242242848428484848484848"), db.findUnique(BigDecimal::class.java, "values (4242242848428484848484848)"))
    }

    @Test
    fun autoDetectingTypes() {
        assertEquals(42, db.findUnique(Any::class.java, "values (42)"))
        assertEquals("foo", db.findUnique(Any::class.java, "values ('foo')"))
        assertEquals(true, db.findUnique(Any::class.java, "values (true)"))
    }

    @Test
    fun constructorRowMapping() {
        val departments = db.findAll(Department::class.java, "select * from (values (1, 'foo'), (2, 'bar')) d")

        assertEquals(2, departments.size)
        assertEquals(1, departments[0].id)
        assertEquals("foo", departments[0].name)
        assertEquals(2, departments[1].id)
        assertEquals("bar", departments[1].name)
    }

    @Test
    fun map() {
        val map = db.findMap(Int::class.javaObjectType, String::class.java, "select * from (values (1, 'foo'), (2, 'bar')) d")

        assertEquals(2, map.size)
        assertEquals("foo", map[1])
        assertEquals("bar", map[2])
    }

    @Test
    fun mapWithNullConversion() {
        val map = db.findMap(String::class.java, String::class.java, "values ('foo', cast (null as clob)), (cast (null as clob), 'bar')")

        assertEquals(2, map.size)
        assertNull(map["foo"])
        assertEquals("bar", map[null])
    }

    @Test
    fun mapWithMultipleArguments() {
        val map = db.findMap(Int::class.javaObjectType, Department::class.java,
                "select * from (values (1, 10, 'foo'), (2, 20, 'bar')) d")

        assertEquals(2, map.size)
        assertEquals(10, map[1]?.id)
        assertEquals("foo", map[1]?.name)
        assertEquals(20, map[2]?.id)
        assertEquals("bar", map[2]?.name)
    }

    @Test
    fun mapWithPrimitiveTypes() {
        val map = db.findMap(Int::class.java, Department::class.java,
                "select * from (values (1, 10, 'foo'), (2, 20, 'bar')) d")

        assertEquals(2, map.size)
        assertEquals(10, map[1]?.id)
        assertEquals("foo", map[1]?.name)
        assertEquals(20, map[2]?.id)
        assertEquals("bar", map[2]?.name)
    }

    @Test
    fun findUnique_singleResult() {
        assertEquals(42, db.findUnique(Int::class.java, "values (42)"))
    }

    @Test
    fun findUnique_nonUniqueResult() {
        assertFailsWith<NonUniqueResultException> {
            db.findUnique(Int::class.java, "VALUES (1), (2)")
        }
    }

    @Test
    fun findUnique_emptyResult() {
        assertFailsWith<EmptyResultException> {
            db.findUnique(Int::class.java, "SELECT * FROM (VALUES (1)) n WHERE FALSE")
        }
    }

    @Test
    fun findUniqueOrNull_singleResult() {
        assertEquals(42, db.findUniqueOrNull(Int::class.java, "values (42)"))
    }

    @Test
    fun findUniqueOrNull_nonUniqueResult() {
        assertFailsWith<NonUniqueResultException> {
            db.findUniqueOrNull(Int::class.java, "values (1), (2)")
        }
    }

    @Test
    fun findUniqueOrNull_emptyResult() {
        assertNull(db.findUniqueOrNull(Int::class.java, "select * from (values (1)) n where false"))
    }

    @Test
    fun findUniqueOrNull_nullResult() {
        assertNull(db.findUniqueOrNull(Int::class.javaObjectType, "values (cast (null as int))"))
    }

    @Test
    fun findOptional_emptyResult() {
        assertEquals(Optional.empty(), db.findOptional(Int::class.java, "select * from (values (1)) n where false"))
    }

    @Test
    fun findOptionalInt_singleResult() {
        assertEquals(OptionalInt.of(42), db.findOptionalInt("values (42)"))
    }

    @Test
    fun findOptionalInt_emptyResult() {
        assertEquals(OptionalInt.empty(), db.findOptionalInt("values (cast (null as int))"))
    }

    @Test
    fun findOptionalLong_singleResult() {
        assertEquals(OptionalLong.of(42), db.findOptionalLong("values (42)"))
    }

    @Test
    fun findOptionalLong_emptyResult() {
        assertEquals(OptionalLong.empty(), db.findOptionalLong("values (cast (null as int))"))
    }

    @Test
    fun findOptionalDouble_singleResult() {
        assertEquals(OptionalDouble.of(42.3), db.findOptionalDouble("values (42.3)"))
    }

    @Test
    fun findOptionalDouble_emptyResult() {
        assertEquals(OptionalDouble.empty(), db.findOptionalDouble("values (cast (null as float))"))
    }

    @Test
    fun findOptional_nonUniqueResult() {
        assertFailsWith<NonUniqueResultException> {
            db.findOptional(Int::class.java, "values (1), (2)")
        }
    }

    @Test
    fun findOptional_nullResult() {
        assertEquals(Optional.empty(), db.findOptional(Int::class.javaObjectType, "values (cast (null as int))"))
    }

    @Test
    fun rowMapper() {
        val squaringRowMapper = RowMapper { resultSet ->
            val value = resultSet.getInt(1)
            value * value
        }

        assertEquals(listOf(1, 4, 9), db.findAll(squaringRowMapper, "values (1), (2), (3)"))
        assertEquals(49, db.findUnique(squaringRowMapper, "values (7)"))
        assertNull(db.findUniqueOrNull(squaringRowMapper, "select * from (values (1)) n where false"))
        assertEquals(Optional.of(49), db.findOptional(squaringRowMapper, "values (7)"))
        assertEquals(Optional.empty(), db.findOptional(squaringRowMapper, "select * from (values (1)) n where false"))
    }

    @Test
    fun customResultProcessor() {
        val rowCounter = ResultSetProcessor { resultSet ->
            var rows = 0
            while (resultSet.next()) rows++
            rows
        }

        assertEquals(3, db.executeQuery(rowCounter, "values (1), (2), (3)"))
    }

    @Test
    fun creatingDatabaseWithJndiDataSourceThrowsExceptionWhenContextIsNotConfigured() {
        assertFailsWith<DatabaseException> {
            Database.forJndiDataSource("foo")
        }
    }

    @Test
    fun implicitTransactions() {
        db.isAllowImplicitTransactions = false
        assertFalse(db.isAllowImplicitTransactions)

        db.isAllowImplicitTransactions = true
        assertTrue(db.isAllowImplicitTransactions)
    }

    @Test
    fun updateUnique_uniqueRow() {
        db.update("drop table if exists unique_test")
        db.update("create table unique_test (id int primary key)")
        db.update("insert into unique_test (id) values (1), (2), (3)")

        db.updateUnique("UPDATE unique_test SET id = 4 WHERE id = 1")
    }

    @Test
    fun updateUnique_missingRow() {
        db.update("drop table if exists unique_test")
        db.update("create table unique_test (id int primary key)")
        db.update("insert into unique_test (id) values (1), (2), (3)")

        assertFailsWith<NonUniqueUpdateException> {
            db.updateUnique("UPDATE unique_test SET id = 4 WHERE id = 5")
        }
    }

    @Test
    fun updateUnique_multipleRows() {
        db.update("drop table if exists unique_test")
        db.update("create table unique_test (id int primary key)")
        db.update("insert into unique_test (id) values (1), (2), (3)")

        assertFailsWith<NonUniqueUpdateException> {
            db.updateUnique("UPDATE unique_test SET id = id+1 WHERE id > 1")
        }
    }

    class Department(val id: Int, val name: String)
}
