/*
 * Copyright (c) 2012 Evident Solutions Oy
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

package fi.evident.dalesbred.utils;

import org.junit.Test;

import static fi.evident.dalesbred.utils.StringUtils.capitalize;
import static fi.evident.dalesbred.utils.StringUtils.upperCamelToLowerUnderscore;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class StringUtilsTest {

    @Test
    public void emptyStringReturnsEmptyString() {
        assertThat(upperCamelToLowerUnderscore(""), is(""));
    }

    @Test
    public void singleWordIsReturnedAsItIs() {
        assertThat(upperCamelToLowerUnderscore("foo"), is("foo"));
    }

    @Test
    public void upperCasedWordsAreConvertedToLowerCase() {
        assertThat(upperCamelToLowerUnderscore("Foo"), is("foo"));
    }

    @Test
    public void multipleWordsAreSeparatedByUnderscores() {
        assertThat(upperCamelToLowerUnderscore("fooBarBaz"), is("foo_bar_baz"));
        assertThat(upperCamelToLowerUnderscore("FooBarBaz"), is("foo_bar_baz"));
    }

    @Test
    public void underscoresAreKept() {
        assertThat(upperCamelToLowerUnderscore("foo_bar"), is("foo_bar"));
        assertThat(upperCamelToLowerUnderscore("FOO_BAR"), is("foo_bar"));
    }

    @Test
    public void consecutiveUpperCaseLettersDoNotBeginNewWord() {
        assertThat(upperCamelToLowerUnderscore("FOO"), is("foo"));
        assertThat(upperCamelToLowerUnderscore("fooBAR"), is("foo_bar"));
    }

    @Test
    public void capitalization() {
        assertThat(capitalize(""), is(""));
        assertThat(capitalize("Foo"), is("Foo"));
        assertThat(capitalize("foo"), is("Foo"));
        assertThat(capitalize("fooBar"), is("FooBar"));
    }
}
