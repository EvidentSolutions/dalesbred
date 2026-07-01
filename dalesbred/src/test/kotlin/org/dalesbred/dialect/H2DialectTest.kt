package org.dalesbred.dialect

import org.dalesbred.Database
import org.dalesbred.testutils.transactionalTest
import kotlin.test.Test
import kotlin.test.assertTrue

class H2DialectTest {

    private val db = Database.forUrlAndCredentials("jdbc:h2:.", "sa", "")

    @Test
    fun `detect dialect`() = transactionalTest(db) { tx ->
        val dialect = Dialect.detect(tx.connection)

        assertTrue { dialect is H2Dialect }
    }
}
