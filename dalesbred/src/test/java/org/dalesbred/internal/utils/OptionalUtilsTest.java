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

import org.junit.Test;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import static org.dalesbred.internal.utils.OptionalUtils.unwrapOptionalAsNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class OptionalUtilsTest {

    @Test
    public void unwrappingNullReturnsNull() {
        assertThat(unwrapOptionalAsNull(null), is(nullValue()));
    }

    @Test
    public void unwrappingNonOptionalsReturnsValueAsItIs() {
        assertThat(unwrapOptionalAsNull("foo"), is("foo"));
        assertThat(unwrapOptionalAsNull(42), is(42));
    }

    @Test
    public void unwrappingEmptyOptionalTypesReturnsNull() {
        assertThat(unwrapOptionalAsNull(Optional.empty()), is(nullValue()));
        assertThat(unwrapOptionalAsNull(OptionalInt.empty()), is(nullValue()));
        assertThat(unwrapOptionalAsNull(OptionalDouble.empty()), is(nullValue()));
        assertThat(unwrapOptionalAsNull(OptionalLong.empty()), is(nullValue()));
    }

    @Test
    public void unwrappingNonEmptyOptionalTypesReturnsContainedValue() {
        assertThat(unwrapOptionalAsNull(Optional.of("foo")), is("foo"));
        assertThat(unwrapOptionalAsNull(Optional.of(42)), is(42));
        assertThat(unwrapOptionalAsNull(OptionalInt.of(42)), is(42));
        assertThat(unwrapOptionalAsNull(OptionalDouble.of(42.2)), is(42.2));
        assertThat(unwrapOptionalAsNull(OptionalLong.of(42L)), is(42L));
    }
}
