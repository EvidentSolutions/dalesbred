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

import org.dalesbred.query.SqlQuery
import org.dalesbred.result.NonUniqueResultException
import org.dalesbred.result.UnexpectedResultException
import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import org.dalesbred.testutils.transactionalTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

@DatabaseTest(POSTGRESQL)
class DatabaseExceptionsTest(private val db: Database) {

    @Test
    fun `findUniqueOrNull non-unique result`() = transactionalTest(db) {
        val query = SqlQuery.query("values (1), (2)")
        val error = assertFailsWith<NonUniqueResultException> {
            db.findUniqueOrNull(Int::class.java, query)
        }
        assertSame(query, error.query)
    }

    @Test
    fun `findOptional non-unique result`() = transactionalTest(db) {
        val query = SqlQuery.query("values (1), (2)")
        val error = assertFailsWith<NonUniqueResultException> {
            db.findOptional(Int::class.java, query)
        }
        assertSame(query, error.query)
    }

    @Test
    fun `findUniqueInt null result`() = transactionalTest(db) {
        val query = SqlQuery.query("values (cast(null as int))")

        val error = assertFailsWith<UnexpectedResultException> {
            db.findUniqueInt(query)
        }
        assertSame(query, error.query)
    }
}
