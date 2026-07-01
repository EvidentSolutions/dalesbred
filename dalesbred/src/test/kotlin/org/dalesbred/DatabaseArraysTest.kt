package org.dalesbred

import org.dalesbred.datatype.SqlArray
import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import org.dalesbred.testutils.transactionalTest
import org.junit.jupiter.api.Assertions.assertArrayEquals
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.test.Test
import kotlin.test.assertEquals

@DatabaseTest(POSTGRESQL)
class DatabaseArraysTest(private val db: Database) {

    @Test
    fun `database arrays as primitive arrays`() = transactionalTest(db) {
        assertArrayEquals(intArrayOf(1, 5, 3), db.findUnique(IntArray::class.java, "values (cast ('{1,5,3}' as numeric array))"))
        assertArrayEquals(longArrayOf(1, 6, 3), db.findUnique(LongArray::class.java, "values (cast ('{1,6,3}' as numeric array))"))
        assertArrayEquals(shortArrayOf(1, 6, 3), db.findUnique(ShortArray::class.java, "values (cast ('{1,6,3}' as numeric array))"))
    }

    @Test
    fun `database arrays as wrapper arrays`() = transactionalTest(db) {
        assertArrayEquals(arrayOf(1, 5, 3), db.findUnique(Array<Int>::class.java, "values (cast ('{1,5,3}' as numeric array))"))
        assertArrayEquals(arrayOf(1L, 6L, 3L), db.findUnique(Array<Long>::class.java, "values (cast ('{1,6,3}' as numeric array))"))
        assertArrayEquals(arrayOf<Short>(1, 6, 3), db.findUnique(Array<Short>::class.java, "values (cast ('{1,6,3}' as numeric array))"))
    }

    @Test
    fun `database arrays for big numbers`() = transactionalTest(db) {
        assertArrayEquals(arrayOf(BigInteger.valueOf(1), BigInteger.valueOf(5L), BigInteger.valueOf(3L)), db.findUnique(Array<BigInteger>::class.java, "values (cast ('{1,5,3}' as numeric array))"))
        assertArrayEquals(arrayOf(BigDecimal.valueOf(1), BigDecimal.valueOf(5L), BigDecimal.valueOf(3L)), db.findUnique(Array<BigDecimal>::class.java, "values (cast ('{1,5,3}' as numeric array))"))
    }

    @Test
    fun `database arrays for strings`() = transactionalTest(db) {
        assertArrayEquals(arrayOf("foo", "bar", "baz"), db.findUnique(Array<String>::class.java, "values (cast ('{foo,bar,baz}' as varchar array))"))
    }

    @Test
    fun `database arrays as lists`() = transactionalTest(db) {
        val obj = db.findUnique(ListContainer::class.java, "select (cast ('{1,5,3}' as numeric array)) as intList, (cast ('{foo, bar, baz}' as varchar array)) as stringList")
        assertEquals(listOf(1, 5, 3), obj.intList)
        assertEquals(listOf("foo", "bar", "baz"), obj.stringList)
    }

    @Test
    fun `database arrays as collections`() = transactionalTest(db) {
        val obj = db.findUnique(CollectionContainer::class.java, "select (cast ('{1,5,3}' as numeric array)) as intCollection, (cast ('{foo, bar, baz}' as varchar array)) as stringCollection")
        assertEquals(listOf(1, 5, 3), obj.intCollection)
        assertEquals(listOf("foo", "bar", "baz"), obj.stringCollection)
    }

    @Test
    fun `database arrays as sets`() = transactionalTest(db) {
        val obj = db.findUnique(SetContainer::class.java, "select (cast ('{1,5,3}' as numeric array)) as intSet, (cast ('{foo, bar, baz}' as varchar array)) as stringSet")
        assertEquals(setOf(1, 5, 3), obj.intSet)
        assertEquals(setOf("foo", "bar", "baz"), obj.stringSet)
    }

    @Test
    fun `bind array`() = transactionalTest(db) {
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
