/*
 * Copyright (c) 2015 Evident Solutions Oy
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

package org.dalesbred.internal.utils;

import org.dalesbred.DatabaseException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EnumUtilsTest {

    private enum TestEnum {
        FOO, BAR, BAZ
    }

    @Test
    public void enumByOrdinal_valid() {
        assertThat(EnumUtils.enumByOrdinal(TestEnum.class, 0), is(TestEnum.FOO));
        assertThat(EnumUtils.enumByOrdinal(TestEnum.class, 1), is(TestEnum.BAR));
        assertThat(EnumUtils.enumByOrdinal(TestEnum.class, 2), is(TestEnum.BAZ));
    }

    @Test(expected = DatabaseException.class)
    public void enumByOrdinal_invalid() {
        EnumUtils.enumByOrdinal(TestEnum.class, 4);
    }
}
