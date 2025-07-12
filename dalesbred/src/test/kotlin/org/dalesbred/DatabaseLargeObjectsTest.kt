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

import org.dalesbred.testutils.transactionalTest
import org.junit.jupiter.api.Assertions.assertArrayEquals
import java.io.InputStream
import java.io.Reader
import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseLargeObjectsTest {

    private val db = TestDatabaseProvider.createInMemoryHSQLDatabase()

    @Test
    fun clobColumnsCanBeCoercedToStrings() = transactionalTest(db) {
        assertEquals("foo", db.findUnique(String::class.java, "values (cast ('foo' as clob))"))
    }

    @Test
    fun blobColumnsCanBeCoercedToStrings() = transactionalTest(db) {
        val data = byteArrayOf(1, 2, 3)
        assertArrayEquals(data, db.findUnique(ByteArray::class.java, "values (cast (? as blob))", data))
    }

    @Test
    fun streamClobToDatabase() = transactionalTest(db) {
        db.update("drop table if exists clob_test")
        db.update("create temporary table clob_test (id int, clob_data clob)")

        val originalData = "foobar"
        db.update("insert into clob_test values (1, ?)", originalData.reader())

        assertEquals(originalData, db.findUnique(String::class.java, "select clob_data from clob_test where id=1"))
    }

    @Test
    fun streamClobFromDatabase() = transactionalTest(db) {
        db.update("drop table if exists clob_test")
        db.update("create temporary table clob_test (id int, clob_data clob)")

        val originalData = "foobar"
        db.update("insert into clob_test values (1, ?)", originalData.reader())

        db.findUnique(Reader::class.java, "select clob_data from clob_test where id=1").use { reader ->
            assertEquals(originalData, reader.readText())
        }
    }

    @Test
    fun streamBlobToDatabase() = transactionalTest(db) {
        db.update("drop table if exists blob_test")
        db.update("create temporary table blob_test (id int, blob_data blob)")

        val originalData = byteArrayOf(25, 35, 3)
        db.update("insert into blob_test values (1, ?)", originalData.inputStream())

        assertArrayEquals(originalData, db.findUnique(ByteArray::class.java, "select blob_data from blob_test where id=1"))
    }

    @Test
    fun streamBlobFromDatabase() = transactionalTest(db) {
        db.update("drop table if exists blob_test")
        db.update("create temporary table blob_test (id int, blob_data blob)")

        val originalData = byteArrayOf(25, 35, 3)
        db.update("insert into blob_test values (1, ?)", originalData.inputStream())

        db.findUnique(InputStream::class.java, "select blob_data from blob_test where id=1").use { stream ->
            assertArrayEquals(originalData, stream.readBytes())
        }
    }
}
