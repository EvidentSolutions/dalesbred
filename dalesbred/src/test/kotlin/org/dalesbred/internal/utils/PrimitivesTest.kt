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
