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

package org.dalesbred.instantiation;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class NamedTypeListTest {

    @Test
    public void readableToString() {
        NamedTypeList.Builder builder = NamedTypeList.builder(3);
        builder.add("foo", String.class);
        builder.add("bar", Integer.class);
        builder.add("baz", Boolean.class);

        NamedTypeList types = builder.build();

        assertEquals("[foo: java.lang.String, bar: java.lang.Integer, baz: java.lang.Boolean]", types.toString());
    }

    @Test
    public void names() {
        NamedTypeList.Builder builder = NamedTypeList.builder(3);
        builder.add("foo", String.class);
        builder.add("bar", Integer.class);
        builder.add("baz", Boolean.class);

        NamedTypeList types = builder.build();

        assertThat(types.getName(0), is("foo"));
        assertThat(types.getName(1), is("bar"));
        assertThat(types.getName(2), is("baz"));
    }

    @Test
    public void types() {
        NamedTypeList.Builder builder = NamedTypeList.builder(3);
        builder.add("foo", String.class);
        builder.add("bar", Integer.class);
        builder.add("baz", Boolean.class);

        NamedTypeList types = builder.build();

        assertThat(types.getType(0), is(type(String.class)));
        assertThat(types.getType(1), is(type(Integer.class)));
        assertThat(types.getType(2), is(type(Boolean.class)));
    }

    @Test
    public void subList() {
        NamedTypeList.Builder builder = NamedTypeList.builder(3);
        builder.add("foo", String.class);
        builder.add("bar", Integer.class);
        builder.add("baz", Boolean.class);

        NamedTypeList types = builder.build().subList(1, 3);

        assertThat(types.size(), is(2));
        assertThat(types.getName(0), is("bar"));
        assertThat(types.getType(0), is(type(Integer.class)));
        assertThat(types.getName(1), is("baz"));
        assertThat(types.getType(1), is(type(Boolean.class)));
    }

    @NotNull
    private static Matcher<Class<?>> type(@NotNull Class<?> cl) {
        return CoreMatchers.<Class<?>>sameInstance(cl);
    }
}
