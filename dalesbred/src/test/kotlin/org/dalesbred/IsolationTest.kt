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

import org.dalesbred.transaction.Isolation.*
import java.sql.Connection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class IsolationTest {

    @Test
    fun levelsMatchJdbcLevels() {
        assertEquals(Connection.TRANSACTION_READ_UNCOMMITTED, READ_UNCOMMITTED.jdbcLevel)
        assertEquals(Connection.TRANSACTION_READ_COMMITTED, READ_COMMITTED.jdbcLevel)
        assertEquals(Connection.TRANSACTION_REPEATABLE_READ, REPEATABLE_READ.jdbcLevel)
        assertEquals(Connection.TRANSACTION_SERIALIZABLE, SERIALIZABLE.jdbcLevel)
    }

    @Test
    fun levelsAreSortedCorrectly() {
        assertEquals(listOf(READ_UNCOMMITTED, READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE),
                listOf(REPEATABLE_READ, SERIALIZABLE, READ_UNCOMMITTED, READ_COMMITTED).sorted())
    }

    @Test
    fun fromJdbcIsolation() {
        assertEquals(READ_UNCOMMITTED, forJdbcCode(Connection.TRANSACTION_READ_UNCOMMITTED))
        assertEquals(READ_COMMITTED, forJdbcCode(Connection.TRANSACTION_READ_COMMITTED))
        assertEquals(REPEATABLE_READ, forJdbcCode(Connection.TRANSACTION_REPEATABLE_READ))
        assertEquals(SERIALIZABLE, forJdbcCode(Connection.TRANSACTION_SERIALIZABLE))
    }

    @Test
    fun fromInvalidJdbcIsolation() {
        assertFailsWith<IllegalArgumentException> {
            forJdbcCode(525)
        }
    }
}
