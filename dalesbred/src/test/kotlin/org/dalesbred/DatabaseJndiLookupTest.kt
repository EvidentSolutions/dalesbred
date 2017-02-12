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

import org.dalesbred.integration.MemoryContext
import org.dalesbred.integration.SystemPropertyRule
import org.junit.Rule
import org.junit.Test
import java.util.*
import javax.naming.Context
import javax.naming.spi.InitialContextFactory
import kotlin.test.assertEquals

class DatabaseJndiLookupTest {

    @get:Rule val rule = SystemPropertyRule("java.naming.factory.initial", MyInitialFactory::class.java.name)

    @Test
    fun createDatabaseByFetchingDataSourceFromJndi() {
        MyInitialFactory.initialContext.bind("java:comp/env/foo", TestDatabaseProvider.createInMemoryHSQLDataSource())

        val db = Database.forJndiDataSource("java:comp/env/foo")
        assertEquals(42, db.findUniqueInt("values (42)"))
    }

    class MyInitialFactory : InitialContextFactory {

        override fun getInitialContext(environment: Hashtable<*, *>): Context = initialContext

        companion object {
            val initialContext = MemoryContext()
        }
    }
}
