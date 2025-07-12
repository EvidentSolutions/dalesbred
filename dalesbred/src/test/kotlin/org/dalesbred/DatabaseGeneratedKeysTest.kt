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

import org.dalesbred.result.ResultSetProcessor
import org.dalesbred.testutils.mapRows
import org.dalesbred.testutils.transactionalTest
import java.sql.ResultSet
import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseGeneratedKeysTest {

    private val db = TestDatabaseProvider.createInMemoryHSQLDatabase()

    @Test
    fun updateWithGeneratedKeys() = transactionalTest(db) {
        db.update("drop table if exists my_table")
        db.update("create temporary table my_table (id identity primary key, my_text varchar(100))")

        val keys = db.updateAndProcessGeneratedKeys(CollectKeysResultSetProcessor, listOf("ID"), "insert into my_table (my_text) values ('foo'), ('bar'), ('baz')")
        assertEquals(listOf(0, 1, 2), keys)
    }

    @Test
    fun updateWithGeneratedKeysWithDefaultColumnNames() = transactionalTest(db) {
        db.update("drop table if exists my_table")
        db.update("create temporary table my_table (id identity primary key, my_text varchar(100))")

        val keys = db.updateAndProcessGeneratedKeys(CollectKeysResultSetProcessor, emptyList(), "insert into my_table (my_text) values ('foo'), ('bar'), ('baz')")
        assertEquals(listOf(0, 1, 2), keys)
    }

    private object CollectKeysResultSetProcessor : ResultSetProcessor<List<Int>> {
        override fun process(resultSet: ResultSet) = resultSet.mapRows { it.getInt((1)) }
    }
}
