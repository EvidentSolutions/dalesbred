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

import org.dalesbred.internal.utils.TypeUtils.arrayType
import org.dalesbred.internal.utils.TypeUtils.rawType
import kotlin.test.Test
import kotlin.test.assertEquals

class TypeUtilsTest {

    @Test
    fun rawTypeOfGenericType() {
        val genericField = ExampleClass::class.java.getField("stringList")
        assertEquals(List::class.java, rawType(genericField.genericType))
        assertEquals(List::class.java, rawType(genericField.type))
    }

    @Test
    fun rawTypeOfArrayType() {
        val genericField = ExampleClass::class.java.getField("stringArray")
        assertType<Array<String>>(rawType(genericField.genericType))
        assertType<Array<String>>(rawType(genericField.type))
    }

    @Test
    fun rawTypeOfSimpleType() {
        val genericField = ExampleClass::class.java.getField("string")
        assertType<String>(rawType(genericField.genericType))
        assertType<String>(rawType(genericField.type))
    }

    @Test
    fun arrayTypes() {
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
