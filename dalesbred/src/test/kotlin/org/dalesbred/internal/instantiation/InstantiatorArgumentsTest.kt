package org.dalesbred.internal.instantiation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class InstantiatorArgumentsTest {

    @Test
    fun `constructor arguments are retained`() {
        val types = NamedTypeList.builder(2).apply {
            add("foo", String::class.java)
            add("bar", Int::class.javaPrimitiveType!!)
        }.build()

        val values = listOf("bar", 4)

        val arguments = InstantiatorArguments(types, values)
        assertSame(types, arguments.types)
        assertEquals(values, arguments.values)
    }

    @Test
    fun `sizes of argument lists differ`() {
        val types = NamedTypeList.builder(2).apply {
            add("foo", String::class.java)
            add("bar", Int::class.javaPrimitiveType!!)
        }.build()

        assertFailsWith<IllegalArgumentException> {
            InstantiatorArguments(types, listOf("bar"))
        }
    }

    @Test
    fun `arguments size`() {
        val types = NamedTypeList.builder(2).apply {
            add("foo", String::class.java)
            add("bar", Int::class.javaPrimitiveType!!)
        }.build()

        val arguments = InstantiatorArguments(types, listOf("bar", 4))
        assertEquals(2, arguments.size())
    }
}
