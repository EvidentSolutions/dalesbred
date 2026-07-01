package org.dalesbred.internal.utils

import org.junit.jupiter.api.Assertions.assertArrayEquals
import kotlin.test.Test
import kotlin.test.assertEquals

class PrimitivesTest {

    @Test
    fun wrapping() {
        assertEquals(Char::class.javaObjectType, Primitives.wrap(Char::class.javaPrimitiveType!!))
        assertEquals(Boolean::class.javaObjectType, Primitives.wrap(Boolean::class.javaPrimitiveType!!))
        assertEquals(Boolean::class.javaObjectType, Primitives.wrap(Boolean::class.javaObjectType))
        assertEquals(String::class.javaObjectType, Primitives.wrap(String::class.java))
    }

    @Test
    fun unwrapping() {
        assertEquals(Char::class.javaPrimitiveType!!, Primitives.unwrap(Char::class.javaObjectType))
        assertEquals(Boolean::class.javaPrimitiveType!!, Primitives.unwrap(Boolean::class.javaObjectType))
        assertEquals(Boolean::class.javaPrimitiveType!!, Primitives.unwrap(Boolean::class.javaPrimitiveType!!))
        assertEquals(String::class.java, Primitives.unwrap(String::class.java))
    }

    @Test
    fun `converting arrays to object array`() {
        assertArrayEquals(arrayOf("foo", "bar"), Primitives.arrayAsObjectArray(arrayOf("foo", "bar")))
        assertArrayEquals(arrayOf(1, 4), Primitives.arrayAsObjectArray(intArrayOf(1, 4)))
    }
}
