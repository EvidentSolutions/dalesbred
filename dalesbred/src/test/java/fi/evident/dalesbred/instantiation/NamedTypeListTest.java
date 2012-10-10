package fi.evident.dalesbred.instantiation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
}
