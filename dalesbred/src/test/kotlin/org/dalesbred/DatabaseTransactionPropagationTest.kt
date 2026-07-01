package org.dalesbred

import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import org.dalesbred.transaction.Propagation.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@DatabaseTest(POSTGRESQL)
class DatabaseTransactionPropagationTest(private val db: Database) {

    @Test
    fun `mandatory propagation without existing transaction throws exception`() {
        assertFailsWith<DatabaseException> {
            db.withTransaction(MANDATORY) { }
        }
    }

    @Test
    fun `mandatory propagation with existing transaction proceeds normally`() {
        val result = db.withTransaction(REQUIRED) {
            db.withTransaction(MANDATORY) {
                "ok"
            }
        }

        assertEquals("ok", result)
    }

    @Test
    fun `nested transactions`() {
        db.update("drop table if exists test_table")
        db.update("create table test_table (text varchar(64))")

        db.withVoidTransaction {
            db.update("INSERT INTO test_table (text) VALUES ('initial')")

            assertEquals("initial", db.findUnique(String::class.java, "SELECT text FROM test_table"))

            assertFailsWith<RuntimeException> {
                db.withVoidTransaction(NESTED) {
                    db.update("UPDATE test_table SET text = 'new-value'")

                    assertEquals("new-value", db.findUnique(String::class.java, "SELECT text FROM test_table"))

                    throw RuntimeException()
                }
            }

            assertEquals("initial", db.findUnique(String::class.java, "SELECT text FROM test_table"))
        }
    }

    @Test
    fun `requires_new suspends active transaction`() {
        db.update("drop table if exists test_table")
        db.update("create table test_table (text varchar(64))")
        db.update("insert into test_table (text) values ('foo')")

        db.withTransaction {
            db.update("update test_table set text='bar'")

            db.withTransaction(REQUIRES_NEW) {
                assertEquals("foo", db.findUnique(String::class.java, "select text from test_table"))
            }

            assertEquals("bar", db.findUnique(String::class.java, "select text from test_table"))
        }
    }
}
