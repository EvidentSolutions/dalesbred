package org.dalesbred

import org.dalesbred.query.SqlQuery.query
import org.dalesbred.result.ResultSetProcessor
import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import org.dalesbred.testutils.mapRows
import org.dalesbred.testutils.transactionalTest
import org.junit.jupiter.api.Assertions.assertArrayEquals
import java.sql.ResultSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

@DatabaseTest(POSTGRESQL)
class DatabaseBatchUpdatesTest(private val db: Database) {

    @Test
    fun `batch update`() = transactionalTest(db) {
        db.update("drop table if exists dictionary")
        db.update("create temporary table dictionary (word varchar(64) primary key)")

        val data = listOf("foo", "bar", "baz").map { listOf(it) }
        val result = db.updateBatch("insert into dictionary (word) values (?)", data)

        assertArrayEquals(intArrayOf(1, 1, 1), result)
        assertEquals(listOf("bar", "baz", "foo"), db.findAll(String::class.java, "select word from dictionary order by word"))
    }

    @Test
    fun `batch update with generated keys`() = transactionalTest(db) {
        db.update("drop table if exists my_table")
        db.update("create temporary table my_table (id serial primary key, str varchar(64), num int)")

        val argLists = listOf(
                listOf("foo", 1),
                listOf("bar", 2),
                listOf("baz", 3))

        val result = db.updateBatchAndProcessGeneratedKeys(CollectKeysResultSetProcessor, listOf("ID"), "INSERT INTO my_table (str, num) VALUES (?,?) returning id", argLists)

        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun `exceptions contain reference to original query`() = transactionalTest(db) {
        val data = listOf(listOf("foo"))

        try {
            db.updateBatch("insert into nonexistent_table (foo) values (?)", data)
            fail("Expected DatabaseException")
        } catch (e: DatabaseException) {
            assertEquals(query("insert into nonexistent_table (foo) values (?)", "<batch-update>"), e.query)
        }
    }

    private object CollectKeysResultSetProcessor : ResultSetProcessor<List<Int>> {
        override fun process(resultSet: ResultSet) = resultSet.mapRows { it.getInt((1)) }
    }
}
