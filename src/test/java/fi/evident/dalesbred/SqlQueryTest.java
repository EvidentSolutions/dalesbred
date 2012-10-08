package fi.evident.dalesbred;

import org.junit.Test;

import static fi.evident.dalesbred.SqlQuery.query;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SqlQueryTest {

    @Test
    public void toStringProvidesMeaningfulInformation() {
        assertThat(query("select bar from foo where id=?", 42, null).toString(),
                   is("select bar from foo where id=? [42, null]"));
    }
}
