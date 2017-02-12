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

import org.dalesbred.datatype.SqlArray
import org.junit.Assert.assertArrayEquals
import org.junit.Rule
import org.junit.Test

import java.math.BigDecimal
import java.math.BigInteger
import kotlin.test.assertEquals

class DatabaseArraysTest {

    private val db = TestDatabaseProvider.createPostgreSQLDatabase()

    @get:Rule val rule = TransactionalTestsRule(db)

    @Test
    fun databaseArraysAsPrimitiveArrays() {
        assertArrayEquals(intArrayOf(1, 5, 3), db.findUnique(IntArray::class.java, "values (cast ('{1,5,3}' as numeric array))"))
        assertArrayEquals(longArrayOf(1, 6, 3), db.findUnique(LongArray::class.java, "values (cast ('{1,6,3}' as numeric array))"))
        assertArrayEquals(shortArrayOf(1, 6, 3), db.findUnique(ShortArray::class.java, "values (cast ('{1,6,3}' as numeric array))"))
    }

    @Test
    fun databaseArraysAsWrapperArrays() {
        assertArrayEquals(arrayOf(1, 5, 3), db.findUnique(Array<Int>::class.java, "values (cast ('{1,5,3}' as numeric array))"))
        assertArrayEquals(arrayOf(1L, 6L, 3L), db.findUnique(Array<Long>::class.java, "values (cast ('{1,6,3}' as numeric array))"))
        assertArrayEquals(arrayOf<Short>(1, 6, 3), db.findUnique(Array<Short>::class.java, "values (cast ('{1,6,3}' as numeric array))"))
    }

    @Test
    fun databaseArraysForBigNumbers() {
        assertArrayEquals(arrayOf(BigInteger.valueOf(1), BigInteger.valueOf(5L), BigInteger.valueOf(3L)), db.findUnique(Array<BigInteger>::class.java, "values (cast ('{1,5,3}' as numeric array))"))
        assertArrayEquals(arrayOf(BigDecimal.valueOf(1), BigDecimal.valueOf(5L), BigDecimal.valueOf(3L)), db.findUnique(Array<BigDecimal>::class.java, "values (cast ('{1,5,3}' as numeric array))"))
    }

    @Test
    fun databaseArraysForStrings() {
        assertArrayEquals(arrayOf("foo", "bar", "baz"), db.findUnique(Array<String>::class.java, "values (cast ('{foo,bar,baz}' as varchar array))"))
    }

    @Test
    fun databaseArraysAsLists() {
        val obj = db.findUnique(ListContainer::class.java, "select (cast ('{1,5,3}' as numeric array)) as intList, (cast ('{foo, bar, baz}' as varchar array)) as stringList")
        assertEquals(listOf(1, 5, 3), obj.intList)
        assertEquals(listOf("foo", "bar", "baz"), obj.stringList)
    }

    @Test
    fun databaseArraysAsCollections() {
        val obj = db.findUnique(CollectionContainer::class.java, "select (cast ('{1,5,3}' as numeric array)) as intCollection, (cast ('{foo, bar, baz}' as varchar array)) as stringCollection")
        assertEquals(listOf(1, 5, 3), obj.intCollection)
        assertEquals(listOf("foo", "bar", "baz"), obj.stringCollection)
    }

    @Test
    fun databaseArraysAsSets() {
        val obj = db.findUnique(SetContainer::class.java, "select (cast ('{1,5,3}' as numeric array)) as intSet, (cast ('{foo, bar, baz}' as varchar array)) as stringSet")
        assertEquals(setOf(1, 5, 3), obj.intSet)
        assertEquals(setOf("foo", "bar", "baz"), obj.stringSet)
    }

    @Test
    fun bindArray() {
        db.update("drop table if exists array_test")
        db.update("create table array_test (string_array VARCHAR array)")

        db.update("insert into array_test (string_array) values (?)", SqlArray.varchars("foo", "bar"))

        assertArrayEquals(arrayOf("foo", "bar"), db.findUnique(Array<String>::class.java, "select string_array from array_test"))
    }

    class ListContainer {
        lateinit var intList: List<Int>
        lateinit var stringList: List<String>
    }

    class SetContainer {
        lateinit var intSet: Set<Int>
        lateinit var stringSet: Set<String>
    }

    class CollectionContainer {
        lateinit var intCollection: Collection<Int>
        lateinit var stringCollection: Collection<String>
    }
}
