package org.dalesbred

import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import kotlin.test.Test
import kotlin.test.assertEquals

@DatabaseTest(POSTGRESQL)
class DatabaseTransactionContextTest(private val db: Database) {

    @Test
    fun `explicit rollback`() {
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
