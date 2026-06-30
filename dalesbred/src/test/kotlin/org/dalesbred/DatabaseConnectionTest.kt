/*
 * Copyright (c) 2026 Evident Solutions Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
