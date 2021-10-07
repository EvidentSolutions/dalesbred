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

import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException
import java.sql.SQLException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ThrowablesTest {

    @Test
    fun propagatingRuntimeExceptionReturnsIt() {
        val exception = RuntimeException()
        assertEquals(exception, Throwables.propagate(exception))
    }

    @Test
    fun propagatingCheckedExceptionWrapsItIntoRuntimeException() {
        val exception = Exception()

        val propagated = Throwables.propagate(exception)
        @Suppress("USELESS_IS_CHECK")
        assertTrue { propagated is RuntimeException }
        assertEquals(exception, propagated.cause)
    }

    @Test
    fun propagatingErrorThrowsIt() {
        val error = MyError()

        try {
            Throwables.propagate(error)
            fail("Expected Error")
        } catch (e: MyError) {
            assertEquals(error, e)
        }
    }

    @Test
    fun propagatingAllowedCheckedExceptionReturnsIt() {
        val exception = IOException()
        assertEquals(exception, Throwables.propagate(exception, IOException::class.java))
    }

    @Test
    fun propagatingDisallowedExceptionThrowsItWrapped() {
        val exception = SQLException()

        try {
            throw Throwables.propagate(exception, IOException::class.java)

        } catch (e: RuntimeException) {
            assertEquals(exception, e.cause)
        }

    }

    class MyError : Error()
}
