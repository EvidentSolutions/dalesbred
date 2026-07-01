package org.dalesbred.internal.instantiation

import kotlin.test.Test
import kotlin.test.assertEquals

class NamedTypeListTest {

    @Test
    fun `readable toString`() {
        val types = NamedTypeList.builder(3).apply {
            add("foo", String::class.java)
            add("bar", Int::class.javaObjectType)
            add("baz", Boolean::class.javaObjectType)
        }.build()

        assertEquals("[foo: java.lang.String, bar: java.lang.Integer, baz: java.lang.Boolean]", types.toString())
    }

    @Test
    fun names() {
        val types = NamedTypeList.builder(3).apply {
            add("foo", String::class.java)
            add("bar", Int::class.java)
            add("baz", Boolean::class.java)
        }.build()

        assertEquals("foo", types.getName(0))
        assertEquals("bar", types.getName(1))
        assertEquals("baz", types.getName(2))
    }

    @Test
    fun types() {
        val types = NamedTypeList.builder(3).apply {
            add("foo", String::class.java)
            add("bar", Int::class.java)
            add("baz", Boolean::class.java)
        }.build()

        assertEquals(String::class.java, types.getType(0))
        assertEquals(Int::class.java, types.getType(1))
        assertEquals(Boolean::class.java, types.getType(2))
    }

    @Test
    fun subList() {
        val types = NamedTypeList.builder(3).apply {
            add("foo", String::class.java)
            add("bar", Int::class.java)
            add("baz", Boolean::class.java)
        }.build().subList(1, 3)

        assertEquals(2, types.size())
        assertEquals("bar", types.getName(0))
        assertEquals(Int::class.java, types.getType(0))
        assertEquals("baz", types.getName(1))
        assertEquals(Boolean::class.java, types.getType(1))
    }
}
