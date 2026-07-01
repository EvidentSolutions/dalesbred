package org.dalesbred.transaction

import org.dalesbred.Database
import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import org.dalesbred.testutils.withConnection
import javax.sql.DataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DatabaseTest(POSTGRESQL)
class SingleConnectionTransactionManagerTest(private val dataSource: DataSource) {

    @Test
    fun `rollbacks without third-party transactions`() {
        dataSource.withConnection { connection ->
            val tm = SingleConnectionTransactionManager(connection, false)
            val db = Database(tm)

            db.update("drop table if exists test_table")
            db.update("create table test_table (text varchar(64))")
            db.update("insert into test_table (text) values ('foo')")

            db.withVoidTransaction { tx ->
                db.update("update test_table set text='bar'")
                tx.setRollbackOnly()
            }

            assertEquals("foo", db.findUnique(String::class.java, "select text from test_table"))
        }
    }

    @Test
    fun `verify hasActiveTransaction`() {
        dataSource.withConnection { connection ->
            val db1 = Database(SingleConnectionTransactionManager(connection, true))
            assertTrue(db1.hasActiveTransaction())

            val db2 = Database(SingleConnectionTransactionManager(connection, false))
            assertFalse(db2.hasActiveTransaction())
        }
    }
}
