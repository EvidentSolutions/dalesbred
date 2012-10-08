package fi.evident.dalesbred.utils;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Test;

import static fi.evident.dalesbred.utils.Primitives.unwrap;
import static fi.evident.dalesbred.utils.Primitives.wrap;
import static org.junit.Assert.assertThat;

public class PrimitivesTest {

    @Test
    public void wrapping() {
        assertThat(wrap(char.class), isClass(Character.class));
        assertThat(wrap(boolean.class), isClass(Boolean.class));
        assertThat(wrap(Boolean.class), isClass(Boolean.class));
        assertThat(wrap(String.class), isClass(String.class));
    }

    @Test
    public void unwrapping() {
        assertThat(unwrap(Character.class), isClass(char.class));
        assertThat(unwrap(Boolean.class), isClass(boolean.class));
        assertThat(unwrap(boolean.class), isClass(boolean.class));
        assertThat(unwrap(String.class), isClass(String.class));
    }

    private static Matcher<Object> isClass(Class<?> cl) {
        return CoreMatchers.is((Object) cl);
    }
}
