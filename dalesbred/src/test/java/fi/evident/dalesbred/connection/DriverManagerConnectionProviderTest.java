package fi.evident.dalesbred.connection;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DriverManagerConnectionProviderTest {

    @Test
    public void defaultConstructorLeavesUserAndPasswordAsNull() {
        DriverManagerConnectionProvider provider = new DriverManagerConnectionProvider("jdbc:example");

        assertEquals("jdbc:example", provider.getUrl());
        assertNull(provider.getUser());
        assertNull(provider.getPassword());

        provider.setUser("foo");
        provider.setPassword("bar");

        assertEquals("foo", provider.getUser());
        assertEquals("bar", provider.getPassword());
    }
}
