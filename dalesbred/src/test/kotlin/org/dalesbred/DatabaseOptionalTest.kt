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
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DatabaseOptionalTest {

    private val db = TestDatabaseProvider.createInMemoryHSQLDatabase()

    @Test
    fun emptyOptionalIsBoundAsNull() = transactionalTest(db) {
        assertNull(db.findUnique(Int::class.javaObjectType, "values (cast (? as int))", Optional.empty<Any>()))
        assertNull(db.findUnique(Int::class.javaObjectType, "values (cast (? as int))", OptionalInt.empty()))
        assertNull(db.findUnique(Long::class.javaObjectType, "values (cast (? as int))", OptionalLong.empty()))
        assertNull(db.findUnique(Double::class.javaObjectType, "values (cast (? as float))", OptionalDouble.empty()))
    }

    @Test
    fun nonEmptyOptionalIsBoundAsValue() = transactionalTest(db) {
        db.update("drop table if exists optional_test")
        db.update("create table optional_test (val VARCHAR(20))")

        db.update("insert into optional_test (val) values (?)", Optional.of("foo"))

        assertEquals(1, db.findUniqueInt("select count(*) from optional_test where val = 'foo'"))
    }

    @Test
    fun nullAreHandledAsEmpty() = transactionalTest(db) {
        val obj = db.findUnique(OptionalContainer::class.java, "select (cast (null as int)) as optionalInt, (cast (null as int)) as optionalInteger, null as optionalString from (values (0))")
        assertEquals(OptionalInt.empty(), obj.optionalInt)
        assertEquals(Optional.empty(), obj.optionalInteger)
        assertEquals(Optional.empty(), obj.optionalString)
    }

    @Test
    fun nonNullsAreHandledAsValues() = transactionalTest(db) {
        val obj = db.findUnique(OptionalContainer::class.java,
                "SELECT 35 AS optionalInt, 53 AS optionalInteger, 'foo' AS optionalString, 4242424 as optionalLong, 424.2 as optionalDouble FROM (VALUES (0))")

        assertEquals(OptionalInt.of(35), obj.optionalInt)
        assertEquals(Optional.of(53), obj.optionalInteger)
        assertEquals(Optional.of("foo"), obj.optionalString)
        assertEquals(OptionalLong.of(4242424), obj.optionalLong)
        assertEquals(OptionalDouble.of(424.2), obj.optionalDouble)
    }

    class OptionalContainer {
        lateinit var optionalInt: OptionalInt
        lateinit var optionalInteger: Optional<Int>
        lateinit var optionalString: Optional<String>
        lateinit var optionalLong: OptionalLong
        lateinit var optionalDouble: OptionalDouble
    }
}
