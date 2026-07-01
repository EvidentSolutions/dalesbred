package org.dalesbred

import org.dalesbred.transaction.Isolation.*
import java.sql.Connection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class IsolationTest {

    @Test
    fun `levels match JDBC levels`() {
        assertEquals(Connection.TRANSACTION_READ_UNCOMMITTED, READ_UNCOMMITTED.jdbcLevel)
        assertEquals(Connection.TRANSACTION_READ_COMMITTED, READ_COMMITTED.jdbcLevel)
        assertEquals(Connection.TRANSACTION_REPEATABLE_READ, REPEATABLE_READ.jdbcLevel)
        assertEquals(Connection.TRANSACTION_SERIALIZABLE, SERIALIZABLE.jdbcLevel)
    }

    @Test
    fun `levels are sorted correctly`() {
        assertEquals(listOf(READ_UNCOMMITTED, READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE),
                listOf(REPEATABLE_READ, SERIALIZABLE, READ_UNCOMMITTED, READ_COMMITTED).sorted())
    }

    @Test
    fun `from JDBC isolation`() {
        assertEquals(READ_UNCOMMITTED, forJdbcCode(Connection.TRANSACTION_READ_UNCOMMITTED))
        assertEquals(READ_COMMITTED, forJdbcCode(Connection.TRANSACTION_READ_COMMITTED))
        assertEquals(REPEATABLE_READ, forJdbcCode(Connection.TRANSACTION_REPEATABLE_READ))
        assertEquals(SERIALIZABLE, forJdbcCode(Connection.TRANSACTION_SERIALIZABLE))
    }

    @Test
    fun `from invalid JDBC isolation`() {
        assertFailsWith<IllegalArgumentException> {
            forJdbcCode(525)
        }
    }
}
