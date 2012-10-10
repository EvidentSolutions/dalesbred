package fi.evident.dalesbred;

import org.junit.Test;

import static fi.evident.dalesbred.SqlQuery.query;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class SqlQueryTest {

    @Test
    public void toStringProvidesMeaningfulInformation() {
        assertThat(query("select bar from foo where id=?", 42, null).toString(),
                   is("select bar from foo where id=? [42, null]"));
    }

    @Test
    public void queriesHaveStructuralEquality() {
        assertThat(query("select * from foo"), is(query("select * from foo")));
        assertThat(query("select * from foo", 1, 2), is(query("select * from foo", 1, 2)));

        assertThat(query("select * from foo"), is(not(query("select * from bar"))));
        assertThat(query("select * from foo", 1, 2), is(not(query("select * from foo", 1, 3))));
    }

    @Test
    public void hashCodeObeysEquality() {
        assertThat(query("select * from foo").hashCode(), is(query("select * from foo").hashCode()));
        assertThat(query("select * from foo", 1, 2).hashCode(), is(query("select * from foo", 1, 2).hashCode()));
    }

    @Test
    public void accessors() {
        SqlQuery query = query("select * from foo", asList(1, 2, 3));

        assertEquals("select * from foo", query.getSql());
        assertEquals(asList(1, 2, 3), query.getArguments());
    }
}
