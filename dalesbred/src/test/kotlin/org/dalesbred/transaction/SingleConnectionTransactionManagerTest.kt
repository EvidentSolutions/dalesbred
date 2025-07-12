/*
 * Copyright (c) 2017 Evident Solutions Oy
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

package org.dalesbred.transaction

import org.dalesbred.Database
import org.dalesbred.TestDatabaseProvider
import org.dalesbred.testutils.withConnection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SingleConnectionTransactionManagerTest {

    private val dataSource = TestDatabaseProvider.createInMemoryHSQLDataSource()

    @Test
    fun rollbacksWithoutThirdPartyTransactions() {
        dataSource.withConnection { connection ->
            val tm = SingleConnectionTransactionManager(connection, false)
            val db = Database(tm)

            db.update("drop table if exists test_table")
            db.update("create table test_table (text varchar(64))")
            db.update("insert into test_table (text) values ('foo')")

            db.withVoidTransaction { tx ->
                db.update("update test_table set text='bar'")
                tx.setRollbackOnly()
            }

            assertEquals("foo", db.findUnique(String::class.java, "select text from test_table"))
        }
    }

    @Test
    fun verifyHasTransaction() {
        dataSource.withConnection { connection ->
            val db1 = Database(SingleConnectionTransactionManager(connection, true))
            assertTrue(db1.hasActiveTransaction())

            val db2 = Database(SingleConnectionTransactionManager(connection, false))
            assertFalse(db2.hasActiveTransaction())
        }
    }
}
