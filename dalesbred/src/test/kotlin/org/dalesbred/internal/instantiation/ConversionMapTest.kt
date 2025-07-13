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
