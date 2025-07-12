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

class NamedTypeListTest {

    @Test
    fun readableToString() {
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
