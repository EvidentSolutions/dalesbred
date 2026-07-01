package org.dalesbred

import org.dalesbred.query.SqlQuery
import org.junit.jupiter.api.AfterEach
import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseExceptionTest {

    @AfterEach
    fun clearDebugContext() {
        DebugContext.setCurrentQuery(null)
    }

    @Test
    fun `query is included in to string`() {
        DebugContext.setCurrentQuery(SqlQuery.query("select * from foo where id=?", 42))
        assertEquals("org.dalesbred.DatabaseException: exception message (query: select * from foo where id=? [42])", DatabaseException("exception message").toString())
    }

    @Test
    fun `if query is not present it is not included`() {
        assertEquals("org.dalesbred.DatabaseException: exception message", DatabaseException("exception message").toString())
    }
}
