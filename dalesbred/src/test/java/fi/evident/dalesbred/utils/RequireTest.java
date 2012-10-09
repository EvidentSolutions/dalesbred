package fi.evident.dalesbred.utils;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RequireTest {

    @Test(expected=NullPointerException.class)
    public void requireNonNullThrowsOnNullValues() {
        Require.requireNonNull(null);
    }

    @Test
    public void requireNonNullReturnsNonNullValues() {
        Object object = new Object();

        assertThat(Require.requireNonNull(object), is(object));
    }
}
