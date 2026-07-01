package org.dalesbred

import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import org.dalesbred.transaction.Isolation.SERIALIZABLE
import org.dalesbred.transaction.TransactionSerializationException
import javax.sql.DataSource
import kotlin.test.Test
import kotlin.test.assertFailsWith

@DatabaseTest(POSTGRESQL)
class DatabaseTransactionIsolationTest(ds: DataSource) {

    private val db1 = Database(ds)
    private val db2 = Database(ds)

    @Test
    fun `concurrent updates in serializable transaction throw TransactionSerializationException`() {
        db1.update("DROP TABLE IF EXISTS isolation_test")
        db1.update("CREATE TABLE isolation_test (value INT)")
        db1.update("INSERT INTO isolation_test (value) VALUES (1)")

        assertFailsWith<TransactionSerializationException> {
            db1.withTransaction(SERIALIZABLE) {
                db1.findUniqueInt("SELECT value FROM isolation_test")

                db2.withTransaction(SERIALIZABLE) {
                    db2.findUniqueInt("SELECT value FROM isolation_test")
                    db2.update("UPDATE isolation_test SET value=2")
                }

                db1.update("UPDATE isolation_test SET value=3")
            }
        }
    }
}
