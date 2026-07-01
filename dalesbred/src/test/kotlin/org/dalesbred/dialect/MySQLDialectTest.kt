package org.dalesbred.dialect

import org.dalesbred.connection.ConnectionProvider
import org.dalesbred.testutils.DatabaseProvider.MYSQL
import org.dalesbred.testutils.DatabaseTest
import kotlin.test.Test
import kotlin.test.assertTrue

@DatabaseTest(MYSQL)
class MySQLDialectTest {

    @Test
    fun `detect my sql dialect`(connectionProvider: ConnectionProvider) {
        val dialect = Dialect.detect(connectionProvider)
        assertTrue { dialect is MySQLDialect }
    }
}
