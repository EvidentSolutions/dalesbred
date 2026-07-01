package org.dalesbred.integration.spring

import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import org.dalesbred.testutils.withConnection
import org.springframework.transaction.support.SimpleTransactionStatus
import javax.sql.DataSource
import kotlin.test.Test
import kotlin.test.assertEquals

@DatabaseTest(POSTGRESQL)
class SpringTransactionContextTest {

    @Test
    fun `rolling back delegates to original contexts`(dataSource: DataSource) {
        dataSource.withConnection { connection ->
            val status = SimpleTransactionStatus()
            val context = SpringTransactionContext(status, connection)

            assertEquals(connection, context.connection)

            assertEquals(false, context.isRollbackOnly)
            assertEquals(false, status.isRollbackOnly)

            context.setRollbackOnly()

            assertEquals(true, context.isRollbackOnly)
            assertEquals(true, status.isRollbackOnly)
        }
    }
}
