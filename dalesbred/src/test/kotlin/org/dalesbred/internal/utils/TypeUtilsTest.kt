package org.dalesbred.internal.utils

import org.dalesbred.internal.utils.TypeUtils.arrayType
import org.dalesbred.internal.utils.TypeUtils.rawType
import kotlin.test.Test
import kotlin.test.assertEquals

class TypeUtilsTest {

    @Test
    fun `raw type of generic type`() {
        val genericField = ExampleClass::class.java.getField("stringList")
        assertEquals(List::class.java, rawType(genericField.genericType))
        assertEquals(List::class.java, rawType(genericField.type))
    }

    @Test
    fun `raw type of array type`() {
        val genericField = ExampleClass::class.java.getField("stringArray")
        assertType<Array<String>>(rawType(genericField.genericType))
        assertType<Array<String>>(rawType(genericField.type))
    }

    @Test
    fun `raw type of simple type`() {
        val genericField = ExampleClass::class.java.getField("string")
        assertType<String>(rawType(genericField.genericType))
        assertType<String>(rawType(genericField.type))
    }

    @Test
    fun `array types`() {
        assertType<Array<String>>(arrayType(String::class.java))
        assertType<Array<Int>>(arrayType(Int::class.javaObjectType))
        assertType<IntArray>(arrayType(Int::class.javaPrimitiveType!!))
    }

    inline fun <reified T : Any> assertType(type: Class<*>) {
        assertEquals(T::class.java, type)
    }

    @Suppress("unused")
    class ExampleClass {
        @JvmField var stringList: List<String>? = null
        @JvmField var stringArray: Array<String>? = null
        @JvmField var string: String? = null
    }
}
