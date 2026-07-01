package org.dalesbred.dialect

import org.dalesbred.connection.ConnectionProvider
import org.dalesbred.testutils.DatabaseProvider.MARIADB
import org.dalesbred.testutils.DatabaseTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DatabaseTest(MARIADB)
class MariaDBDialectTest {

    @Test
    fun `detect MariaDBDialect`(connectionProvider: ConnectionProvider) {
        val dialect = Dialect.detect(connectionProvider)
        assertTrue { dialect is MariaDBDialect }
    }

    @Test
    fun `overrideResultSetMetaDataType should be empty for MariaDB ConnectorJ 3_5_1 or earlier`() {
        val dialect = MariaDBDialect().apply { setBlobsAsByteArrays(false) }
        assertNull(dialect.overrideResultSetMetaDataType("java.sql.Blob"))
    }

    @Test
    fun `overrideResultSetMetaDataType should not be empty for MariaDB ConnectorJ 3_5_2 or later`() {
        val dialect = MariaDBDialect().apply { setBlobsAsByteArrays(true) }
        assertEquals(ByteArray::class.java, dialect.overrideResultSetMetaDataType("java.sql.Blob"))
    }

}
