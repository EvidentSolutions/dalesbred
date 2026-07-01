package org.dalesbred.integration.kotlin

import org.dalesbred.Database
import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import org.dalesbred.testutils.transactionalTest
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@DatabaseTest(POSTGRESQL)
class DatabaseExtensionsTest(private val db: Database) {

    @Test
    fun findAll() = transactionalTest(db) {
        assertEquals(listOf(Department(1, "foo"), Department(2, "bar")),
                db.findAll<Department>("select * from (values (1, 'foo'), (2, 'bar')) d"))
    }

    @Test
    fun `findAll customMapper`() = transactionalTest(db) {
        val result = db.findAll("select * from (values (1, 'foo'), (2, 'bar')) d") { rs ->
            rs.getInt(1) to rs.getString(2).reversed()
        }

        assertEquals(listOf(1 to "oof", 2 to "rab"), result)
    }

    @Test
    fun findUnique() = transactionalTest(db) {
        assertEquals(Department(1, "foo"), db.findUnique<Department>("select * from (values (1, 'foo')) d"))
    }

    @Test
    fun `findUnique customMapper`() = transactionalTest(db) {
        val result = db.findUnique("select * from (values (1, 'foo')) d") { rs ->
            rs.getInt(1) to rs.getString(2).reversed()
        }

        assertEquals(1 to "oof", result)
    }

    @Test
    fun `findUniqueOrNull existing`() = transactionalTest(db) {
        assertEquals(Department(1, "foo"), db.findUniqueOrNull<Department>("select * from (values (1, 'foo')) d"))
    }

    @Test
    fun `findUniqueOrNull nonexistent`() = transactionalTest(db) {
        assertNull(db.findUniqueOrNull<Department>("select * from (values (1, 'foo')) d where 1 = 2"))
    }

    @Test
    fun `findUniqueOrNull customMapper`() = transactionalTest(db) {
        val result = db.findUniqueOrNull("select * from (values (1, 'foo')) d") { rs ->
            rs.getInt(1) to rs.getString(2).reversed()
        }

        assertEquals(1 to "oof", result)
    }

    @Test
    fun `findOptional existing`() = transactionalTest(db) {
        assertEquals(Optional.of(Department(1, "foo")), db.findOptional<Department>("select * from (values (1, 'foo')) d"))
    }

    @Test
    fun `findOptional nonexistent`() = transactionalTest(db) {
        assertEquals(Optional.empty(), db.findOptional<Department>("select * from (values (1, 'foo')) d where 1 = 2"))
    }

    @Test
    fun `findOptional customMapper`() = transactionalTest(db) {
        val result = db.findOptional("select * from (values (1, 'foo')) d") { rs ->
            rs.getInt(1) to rs.getString(2).reversed()
        }

        assertEquals(Optional.of(1 to "oof"), result)
    }

    @Test
    fun findMap() = transactionalTest(db) {
        val map = db.findMap<Int,String>("select * from (values (1, 'foo'), (2, 'bar')) d")

        assertEquals(mapOf(1 to "foo", 2 to "bar"), map)
    }

    @Test
    fun executeQuery() = transactionalTest(db) {
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
}
