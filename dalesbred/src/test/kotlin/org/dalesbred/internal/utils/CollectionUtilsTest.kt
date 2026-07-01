package org.dalesbred.internal.utils

import org.dalesbred.internal.utils.CollectionUtils.arrayOfType
import org.dalesbred.internal.utils.CollectionUtils.mapToList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CollectionUtilsTest {

    @Test
    fun mapping() {
        assertEquals(listOf(3, 4, 5), mapToList(listOf("foo", "quux", "xyzzy")) { it.length })
    }

    @Test
    fun `create array of object type`() {
        val array = arrayOfType(Int::class.javaObjectType, listOf(1, 42, 7))

        assertTrue { Array<Int>::class.java.isInstance(array) }

        @Suppress("UNCHECKED_CAST")
        val intArray = array as Array<Int>
        assertEquals(3, intArray.size)
        assertEquals(1, intArray[0])
        assertEquals(42, intArray[1])
        assertEquals(7, intArray[2])
    }

    @Test
    fun `create array of primitive type`() {
        val array = arrayOfType(Int::class.javaPrimitiveType!!, listOf(1, 42, 7))

        assertTrue { array is IntArray }

        val intArray = array as IntArray
        assertEquals(3, intArray.size)
        assertEquals(1, intArray[0])
        assertEquals(42, intArray[1])
        assertEquals(7, intArray[2])
    }
}
