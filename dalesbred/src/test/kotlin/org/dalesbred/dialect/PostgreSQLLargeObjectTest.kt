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

@file:Suppress("SqlResolve")

package org.dalesbred.dialect

import org.dalesbred.Database
import org.dalesbred.datatype.InputStreamWithSize
import org.dalesbred.datatype.ReaderWithSize
import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import org.dalesbred.testutils.transactionalTest
import org.junit.jupiter.api.Assertions.assertArrayEquals
import kotlin.test.Test
import kotlin.test.assertEquals

@DatabaseTest(POSTGRESQL)
class PostgreSQLLargeObjectTest(private val db: Database) {

    @Test
    fun `stream blob to database byte array`() = transactionalTest(db) {
        db.update("drop table if exists blob_test")
        db.update("create temporary table blob_test (id int, blob_data bytea)")

        val originalData = byteArrayOf(25, 35, 3)
        db.update("insert into blob_test values (1, ?)", InputStreamWithSize(originalData.inputStream(), originalData.size.toLong()))

        val data = db.findUnique(ByteArray::class.java, "select blob_data from blob_test where id=1")
        assertArrayEquals(originalData, data)
    }

    @Test
    fun `stream reader to database text`() = transactionalTest(db) {
        db.update("drop table if exists text_test")
        db.update("create temporary table text_test (id int, text_data text)")

        val originalData = "foo"
        db.update("insert into text_test values (1, ?)", ReaderWithSize(originalData.reader(), originalData.length.toLong()))

        val data = db.findUnique(String::class.java, "select text_data from text_test where id=1")
        assertEquals(originalData, data)
    }
}
