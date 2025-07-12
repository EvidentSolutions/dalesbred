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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class InstantiatorArgumentsTest {

    @Test
    fun constructorArgumentsAreRetained() {
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
    fun sizesOfArgumentListsDiffer() {
        val types = NamedTypeList.builder(2).apply {
            add("foo", String::class.java)
            add("bar", Int::class.javaPrimitiveType!!)
        }.build()

        assertFailsWith<IllegalArgumentException> {
            InstantiatorArguments(types, listOf("bar"))
        }
    }

    @Test
    fun argumentsSize() {
        val types = NamedTypeList.builder(2).apply {
            add("foo", String::class.java)
            add("bar", Int::class.javaPrimitiveType!!)
        }.build()

        val arguments = InstantiatorArguments(types, listOf("bar", 4))
        assertEquals(2, arguments.size())
    }
}
