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

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static fi.evident.dalesbred.utils.Throwables.propagate;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ThrowablesTest {

    @Test
    public void propagatingRuntimeExceptionReturnsIt() {
        RuntimeException exception = new RuntimeException();
        assertThat(propagate(exception), is(exception));
    }

    @Test
    public void propagatingCheckedExceptionWrapsItIntoRuntimeException() {
        Exception exception = new Exception();

        Exception propagated = propagate(exception);
        assertThat(propagated, is(RuntimeException.class));
        assertThat(propagated.getCause(), CoreMatchers.<Throwable>is(exception));
    }

    @Test
    public void propagatingErrorThrowsIt() {
        MyError error = new MyError();
        try {
            RuntimeException result = propagate(error);
            fail("Expected Error");
            throw result;
        } catch (MyError e) {
            assertThat(e, is(error));
        }
    }

    @Test
    public void propagatingAllowedCheckedExceptionReturnsIt() {
        IOException exception = new IOException();
        assertThat(propagate(exception, IOException.class), is(exception));
    }

    @Test
    public void propagatingDisallowedExceptionThrowsItWrapped() throws IOException {
        SQLException exception = new SQLException();

        try {
            throw propagate(exception, IOException.class);

        } catch (RuntimeException e) {
            assertThat(e.getCause(), CoreMatchers.<Throwable>is(exception));
        }
    }

    static class MyError extends Error { }
}
