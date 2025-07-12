package org.dalesbred.internal.jdbc

import org.dalesbred.dialect.DefaultDialect
import org.dalesbred.dialect.MariaDBDialect
import org.dalesbred.testutils.unimplemented
import java.sql.Blob
import java.sql.ResultSetMetaData
import kotlin.test.Test
import kotlin.test.assertEquals

class ResultSetUtilsTest {

    companion object {
        private val DEFAULT_DIALECT = DefaultDialect()
        private val MARIADB_DIALECT_OLD = MariaDBDialect().apply { setBlobsAsByteArrays(false) }
        private val MARIADB_DIALECT_CURRENT = MariaDBDialect().apply { setBlobsAsByteArrays(true) }
    }

    @Test
    fun `column type resolution`() {
        val metadata = MockResultSetMetaData(
            classNames = listOf("java.lang.String", "[Ljava.lang.String;", "[B")
        )
        assertEquals(String::class.java, ResultSetUtils.getColumnType(metadata, DEFAULT_DIALECT, 1))
        assertEquals(Array<String>::class.java, ResultSetUtils.getColumnType(metadata, DEFAULT_DIALECT, 2))
        assertEquals(ByteArray::class.java, ResultSetUtils.getColumnType(metadata, DEFAULT_DIALECT, 3))
    }

    @Test
    fun `resolving byte array types for MariaDB`() {
        val metadata = MockResultSetMetaData(classNames = listOf("byte[]"))
        assertEquals(ByteArray::class.java, ResultSetUtils.getColumnType(metadata, MARIADB_DIALECT_OLD, 1))
        assertEquals(ByteArray::class.java, ResultSetUtils.getColumnType(metadata, MARIADB_DIALECT_CURRENT, 1))
    }

    @Test
    fun `resolving binary blob types for MariaDB ConnectorJ 3_5_1 or earlier`() {
        val metadata = MockResultSetMetaData(classNames = listOf("java.sql.Blob"))
        assertEquals(Blob::class.java, ResultSetUtils.getColumnType(metadata, MARIADB_DIALECT_OLD, 1))
    }

    @Test
    fun `resolving binary blob types for MariaDB ConnectorJ 3_5_2 or later`() {
        val metadata = MockResultSetMetaData(classNames = listOf("java.sql.Blob"))
        assertEquals(ByteArray::class.java, ResultSetUtils.getColumnType(metadata, MARIADB_DIALECT_CURRENT, 1))
    }

    class MockResultSetMetaData(val classNames: List<String>) : ResultSetMetaData by unimplemented() {
        override fun getColumnCount() = classNames.size
        override fun getColumnClassName(column: Int) = classNames[column - 1]
    }
}
