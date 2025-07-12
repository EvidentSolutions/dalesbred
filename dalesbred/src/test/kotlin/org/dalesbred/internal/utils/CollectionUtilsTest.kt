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

import org.dalesbred.internal.utils.CollectionUtils.arrayOfType
import org.dalesbred.internal.utils.CollectionUtils.mapToList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CollectionUtilsTest {

    @Test
    fun mapping() {
        assertEquals(listOf(3, 4, 5), mapToList(listOf("foo", "quux", "xyzzy")) { it.length })
    }

    @Test
    fun createArrayOfObjectType() {
        val array = arrayOfType(Int::class.javaObjectType, listOf(1, 42, 7))

        assertTrue { Array<Int>::class.java.isInstance(array) }

        @Suppress("UNCHECKED_CAST")
        val intArray = array as Array<Int>
        assertEquals(3, intArray.size)
        assertEquals(1, intArray[0])
        assertEquals(42, intArray[1])
        assertEquals(7, intArray[2])
    }

    @Test
    fun createArrayOfPrimitiveType() {
        val array = arrayOfType(Int::class.javaPrimitiveType!!, listOf(1, 42, 7))

        assertTrue { array is IntArray }

        val intArray = array as IntArray
        assertEquals(3, intArray.size)
        assertEquals(1, intArray[0])
        assertEquals(42, intArray[1])
        assertEquals(7, intArray[2])
    }
}
