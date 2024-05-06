package org.dalesbred.internal.jdbc

import org.dalesbred.testutils.unimplemented
import org.junit.Test
import java.sql.ResultSetMetaData
import kotlin.test.assertEquals

class ResultSetUtilsTest {

    @Test
    fun `column type resolution`() {
        val metadata = MockResultSetMetaData(
            classNames = listOf("java.lang.String", "[Ljava.lang.String;", "[B")
        )
        assertEquals(String::class.java, ResultSetUtils.getColumnType(metadata, 1))
        assertEquals(Array<String>::class.java, ResultSetUtils.getColumnType(metadata, 2))
        assertEquals(ByteArray::class.java, ResultSetUtils.getColumnType(metadata, 3))
    }

    @Test
    fun `resolving byte array types for MariaDB`() {
        val metadata = MockResultSetMetaData(classNames = listOf("byte[]"))
        assertEquals(ByteArray::class.java, ResultSetUtils.getColumnType(metadata, 1))
    }

    class MockResultSetMetaData(val classNames: List<String>) : ResultSetMetaData by unimplemented() {
        override fun getColumnCount() = classNames.size
        override fun getColumnClassName(column: Int) = classNames[column - 1]
    }
}
