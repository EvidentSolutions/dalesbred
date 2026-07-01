package org.dalesbred

import org.dalesbred.result.ResultSetProcessor
import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import org.dalesbred.testutils.mapRows
import org.dalesbred.testutils.transactionalTest
import java.sql.ResultSet
import kotlin.test.Test
import kotlin.test.assertEquals

@DatabaseTest(POSTGRESQL)
class DatabaseGeneratedKeysTest(private val db: Database) {

    @Test
    fun `update with generated keys`() = transactionalTest(db) {
        db.update("drop table if exists my_table")
        db.update("create temporary table my_table (id serial primary key, my_text varchar(100))")

        val keys = db.updateAndProcessGeneratedKeys(CollectKeysResultSetProcessor, listOf("ID"), "insert into my_table (my_text) values ('foo'), ('bar'), ('baz') returning id")
        assertEquals(listOf(1, 2, 3), keys)
    }

    @Test
    fun `update with generated keys with default column names`() = transactionalTest(db) {
        db.update("drop table if exists my_table")
        db.update("create temporary table my_table (id serial primary key, my_text varchar(100))")

        val keys = db.updateAndProcessGeneratedKeys(CollectKeysResultSetProcessor, emptyList(), "insert into my_table (my_text) values ('foo'), ('bar'), ('baz') returning id")
        assertEquals(listOf(1, 2, 3), keys)
    }

    private object CollectKeysResultSetProcessor : ResultSetProcessor<List<Int>> {
        override fun process(resultSet: ResultSet) = resultSet.mapRows { it.getInt((1)) }
    }
}
