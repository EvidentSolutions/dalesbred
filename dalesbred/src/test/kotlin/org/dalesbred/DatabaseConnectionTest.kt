package org.dalesbred

import org.dalesbred.connection.DataSourceConnectionProvider
import org.dalesbred.dialect.Dialect
import org.dalesbred.testutils.DatabaseProvider.HSQL
import org.dalesbred.testutils.DatabaseTest
import org.junit.jupiter.api.BeforeEach
import javax.sql.DataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

@DatabaseTest(HSQL)
class DatabaseConnectionTest(dataSource: DataSource) {

    private val source = DatabaseSource(dataSource, Dialect.detect(DataSourceConnectionProvider(dataSource)))

    @BeforeEach
    fun createTable() {
        source.openConnection().use { conn ->
            conn.update("drop table if exists dc_test")
            conn.update("create table dc_test (value varchar(64))")
        }
    }

    @Test
    fun `changes are committed on close`() {
        source.openConnection().use { conn ->
            conn.update("insert into dc_test values ('hello')")
        }

        source.openConnection().use { conn ->
            assertEquals("hello", conn.findUnique(String::class.java, "select value from dc_test"))
        }
    }

    @Test
    fun `changes are rolled back on close when marked rollback-only`() {
        source.openConnection().use { conn ->
            conn.update("insert into dc_test values ('rolled_back')")
            conn.setRollbackOnly()
        }
        source.openConnection().use { conn ->
            assertEquals(0, conn.findAll(String::class.java, "select value from dc_test").size)
        }
    }

    @Test
    fun `multiple operations share the same connection`() {
        source.openConnection().use { conn ->
            conn.update("insert into dc_test values ('foo')")
            conn.update("insert into dc_test values ('bar')")
        }

        source.openConnection().use { conn ->
            assertEquals(2, conn.findAll(String::class.java, "select value from dc_test").size)
        }
    }

    @Test
    fun `exception during query does not prevent close from committing`() {
        source.openConnection().use { conn ->
            conn.update("insert into dc_test values ('committed')")
            assertFails { conn.findUnique(String::class.java, "select value from nonexistent_table") }
        }

        // The insert before the failing query was committed (no automatic rollback on exception)
        source.openConnection().use { conn ->
            assertEquals(1, conn.findAll(String::class.java, "select value from dc_test").size)
        }
    }
}
