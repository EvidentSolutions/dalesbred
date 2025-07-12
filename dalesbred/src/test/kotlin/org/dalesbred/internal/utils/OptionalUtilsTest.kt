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

import org.dalesbred.internal.utils.OptionalUtils.unwrapOptionalAsNull
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OptionalUtilsTest {

    @Test
    fun unwrappingNullReturnsNull() {
        assertNull(unwrapOptionalAsNull(null))
    }

    @Test
    fun unwrappingNonOptionalsReturnsValueAsItIs() {
        assertEquals("foo", unwrapOptionalAsNull("foo"))
        assertEquals(42, unwrapOptionalAsNull(42))
    }

    @Test
    fun unwrappingEmptyOptionalTypesReturnsNull() {
        assertNull(unwrapOptionalAsNull(Optional.empty<Any>()))
        assertNull(unwrapOptionalAsNull(OptionalInt.empty()))
        assertNull(unwrapOptionalAsNull(OptionalDouble.empty()))
        assertNull(unwrapOptionalAsNull(OptionalLong.empty()))
    }

    @Test
    fun unwrappingNonEmptyOptionalTypesReturnsContainedValue() {
        assertEquals("foo", unwrapOptionalAsNull(Optional.of("foo")))
        assertEquals(42, unwrapOptionalAsNull(Optional.of(42)))
        assertEquals(42, unwrapOptionalAsNull(OptionalInt.of(42)))
        assertEquals(42.2, unwrapOptionalAsNull(OptionalDouble.of(42.2)))
        assertEquals(42L, unwrapOptionalAsNull(OptionalLong.of(42L)))
    }
}
