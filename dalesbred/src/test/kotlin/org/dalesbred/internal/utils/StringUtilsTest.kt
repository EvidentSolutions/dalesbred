/*
 * Copyright (c) 2018 Evident Solutions Oy
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

import org.dalesbred.internal.utils.StringUtils.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringUtilsTest {

    @Test
    fun capitalization() {
        assertEquals("", capitalize(""))
        assertEquals("Foo", capitalize("Foo"))
        assertEquals("Foo", capitalize("foo"))
        assertEquals("FooBar", capitalize("fooBar"))
    }

    @Test
    fun `case and underscore ignoring equality`() {
        assertTrue(isEqualIgnoringCaseAndUnderscores("", ""))
        assertTrue(isEqualIgnoringCaseAndUnderscores("foo", "foo"))
        assertFalse(isEqualIgnoringCaseAndUnderscores("foo", "bar"))

        assertTrue(isEqualIgnoringCaseAndUnderscores("foo", "FOo"))
        assertTrue(isEqualIgnoringCaseAndUnderscores("Foo", "FOo"))

        assertTrue(isEqualIgnoringCaseAndUnderscores("Foo_bar", "FOoBar"))
        assertTrue(isEqualIgnoringCaseAndUnderscores("Foobar", "FOo_Bar"))

        assertTrue(isEqualIgnoringCaseAndUnderscores("_foo_", "foo"))
        assertTrue(isEqualIgnoringCaseAndUnderscores("foo", "__foo__"))
    }

    @Test
    fun `rightPad - no need to pad`() {
        assertEquals("", rightPad("", 0, ' '))
        assertEquals("foo", rightPad("foo", 0, ' '))
        assertEquals("foo", rightPad("foo", 2, ' '))
        assertEquals("foo", rightPad("foo", 3, ' '))
    }

    @Test
    fun `rightPad - padding`() {
        assertEquals("    ", rightPad("", 4, ' '))
        assertEquals("foo ", rightPad("foo", 4, ' '))
        assertEquals("foo-", rightPad("foo", 4, '-'))
    }

    @Test
    fun `truncate - no need to truncate`() {
        assertEquals("", truncate("", 4))
        assertEquals("foo", truncate("foo", 4))
        assertEquals("quux", truncate("quux", 4))
    }

    @Test
    fun `truncate - truncation needed`() {
        assertEquals("foo...", truncate("foobar-baz", 6))
    }

    @Test
    fun `truncate - suffix longer than truncation length`() {
        assertEquals("..", truncate("foobar", 2))
    }
}
