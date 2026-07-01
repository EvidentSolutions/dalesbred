package org.dalesbred.internal.instantiation

import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ConversionMapTest {

    private val registry = ConversionMap()

    @Test
    fun `searching for not existing item returns null`() {
        assertEquals(Optional.empty(), registry.findConversion(Int::class.java, String::class.java))
    }

    @Test
    fun `search based on exact match`() {
        val conversion = dummyConversion()
        registry.register(Int::class.java, String::class.java, conversion)

        assertSame(conversion, registry.findConversion(Int::class.java, String::class.java).orElse(null))
    }

    @Test
    fun `search based on result covariance`() {
        val conversion = dummyConversion()
        registry.register(Int::class.java, String::class.java, conversion)

        assertSame(conversion, registry.findConversion(Int::class.java, Any::class.java).orElse(null))
    }

    @Test
    fun `search based on param contravariance`() {
        val conversion = dummyConversion()
        registry.register(Number::class.java, String::class.java, conversion)

        assertSame(conversion, registry.findConversion(Int::class.java, String::class.java).orElse(null))
    }

    @Test
    fun `primitives and wrappers are considered same`() {
        val conversion = dummyConversion()
        registry.register(Int::class.java, Long::class.java, conversion)

        assertSame(conversion, registry.findConversion(Int::class.javaPrimitiveType!!, Long::class.javaPrimitiveType!!).orElse(null))
    }

    @Test
    fun `source contravariance on interfaces`() {
        val conversion = dummyConversion()
        registry.register(CharSequence::class.java, Long::class.java, conversion)

        assertSame(conversion, registry.findConversion(String::class.java, Long::class.java).orElse(null))
    }

    @Test
    fun `later additions override earlier ones`() {
        val conversion1 = dummyConversion()
        val conversion2 = dummyConversion()
        registry.register(String::class.java, Long::class.java, conversion1)
        registry.register(String::class.java, Long::class.java, conversion2)

        assertSame(conversion2, registry.findConversion(String::class.java, Long::class.java).orElse(null))
    }

    private fun dummyConversion(): TypeConversion {
        return TypeConversion.fromNonNullFunction<Any, Any> { throw UnsupportedOperationException() }
    }
}
