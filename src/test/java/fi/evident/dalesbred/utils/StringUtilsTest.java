package fi.evident.dalesbred.utils;

import org.junit.Test;

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
}
