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

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PrimitivesTest {

    @Test
    public void wrapping() {
        assertThat(Primitives.wrap(char.class), sameClass(Character.class));
        assertThat(Primitives.wrap(boolean.class), sameClass(Boolean.class));
        assertThat(Primitives.wrap(Boolean.class), sameClass(Boolean.class));
        assertThat(Primitives.wrap(String.class), sameClass(String.class));
    }

    @Test
    public void unwrapping() {
        assertThat(Primitives.unwrap(Character.class), sameClass(char.class));
        assertThat(Primitives.unwrap(Boolean.class), sameClass(boolean.class));
        assertThat(Primitives.unwrap(boolean.class), sameClass(boolean.class));
        assertThat(Primitives.unwrap(String.class), sameClass(String.class));
    }

    @Test
    public void convertingArraysToObjectArray() {
        assertThat(Primitives.arrayAsObjectArray(new String[]{"foo", "bar"}), CoreMatchers.<Object[]>is(new String[]{"foo", "bar"}));
        assertThat(Primitives.arrayAsObjectArray(new int[]{1, 4}), CoreMatchers.<Object[]>is(new Integer[]{1, 4}));
    }

    private static Matcher<Object> sameClass(Class<?> cl) {
        return is((Object) cl);
    }
}
