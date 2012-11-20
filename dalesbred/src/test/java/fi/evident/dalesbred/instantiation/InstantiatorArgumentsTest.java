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

package fi.evident.dalesbred.instantiation;

import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class InstantiatorArgumentsTest {

    @Test
    public void constructorArgumentsAreRetained() {
        NamedTypeList types = NamedTypeList.builder(2).add("foo", String.class).add("bar", int.class).build();
        List<?> values = asList("bar", 4);

        InstantiatorArguments arguments = new InstantiatorArguments(types, values);
        assertSame(types, arguments.getTypes());
        assertEquals(values, arguments.getValues());
    }

    @Test(expected = IllegalArgumentException.class)
    public void sizesOfArgumentListsDiffer() {
        NamedTypeList types = NamedTypeList.builder(2).add("foo", String.class).add("bar", int.class).build();
        new InstantiatorArguments(types, singletonList("bar"));
    }
}
