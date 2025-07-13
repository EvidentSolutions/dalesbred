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

package org.dalesbred

import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import org.dalesbred.transaction.Propagation.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@DatabaseTest(POSTGRESQL)
class DatabaseTransactionPropagationTest(private val db: Database) {

    @Test
    fun `mandatory propagation without existing transaction throws exception`() {
        assertFailsWith<DatabaseException> {
            db.withTransaction(MANDATORY) { }
        }
    }

    @Test
    fun `mandatory propagation with existing transaction proceeds normally`() {
        val result = db.withTransaction(REQUIRED) {
            db.withTransaction(MANDATORY) {
                "ok"
            }
        }

        assertEquals("ok", result)
    }

    @Test
    fun `nested transactions`() {
        db.update("drop table if exists test_table")
        db.update("create table test_table (text varchar(64))")

        db.withVoidTransaction {
            db.update("INSERT INTO test_table (text) VALUES ('initial')")

            assertEquals("initial", db.findUnique(String::class.java, "SELECT text FROM test_table"))

            assertFailsWith<RuntimeException> {
                db.withVoidTransaction(NESTED) {
                    db.update("UPDATE test_table SET text = 'new-value'")

                    assertEquals("new-value", db.findUnique(String::class.java, "SELECT text FROM test_table"))

                    throw RuntimeException()
                }
            }

            assertEquals("initial", db.findUnique(String::class.java, "SELECT text FROM test_table"))
        }
    }

    @Test
    fun `requires_new suspends active transaction`() {
        db.update("drop table if exists test_table")
        db.update("create table test_table (text varchar(64))")
        db.update("insert into test_table (text) values ('foo')")

        db.withTransaction {
            db.update("update test_table set text='bar'")

            db.withTransaction(REQUIRES_NEW) {
                assertEquals("foo", db.findUnique(String::class.java, "select text from test_table"))
            }

            assertEquals("bar", db.findUnique(String::class.java, "select text from test_table"))
        }
    }
}
