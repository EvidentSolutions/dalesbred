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
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import kotlin.test.fail

class DatabaseExceptionsTest {

    private val db = TestDatabaseProvider.createInMemoryHSQLDatabase()

    @get:Rule val rule = TransactionalTestsRule(db)

    @Test
    fun findUniqueOrNull_nonUniqueResult() {
        val query = SqlQuery.query("values (1), (2)")
        try {
            db.findUniqueOrNull(Int::class.java, query)
            fail("Expected NonUniqueResultException")
        } catch (e: NonUniqueResultException) {
            assertSame(query, e.query)
        }
    }

    @Test
    fun findOptional_nonUniqueResult() {
        val query = SqlQuery.query("values (1), (2)")
        try {
            db.findOptional(Int::class.java, query)
            fail("Expected NonUniqueResultException")
        } catch (e: NonUniqueResultException) {
            assertSame(query, e.query)
        }
    }

    @Test
    fun findUniqueInt_nullResult() {
        val query = SqlQuery.query("values (cast(null as int))")
        try {
            db.findUniqueInt(query)
            fail("Expected UnexpectedResultException")
        } catch (e: UnexpectedResultException) {
            assertSame(query, e.query)
        }
    }
}
