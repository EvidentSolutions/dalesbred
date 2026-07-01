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
