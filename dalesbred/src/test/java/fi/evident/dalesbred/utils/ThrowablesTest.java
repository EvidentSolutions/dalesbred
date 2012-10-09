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
