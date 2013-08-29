package fi.evident.dalesbred;

import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DatabaseLargeObjectsTest {

    private final Database db = TestDatabaseProvider.createInMemoryHSQLDatabase();

    @Rule
    public final TransactionalTestsRule rule = new TransactionalTestsRule(db);

    @Test
    public void clobColumnsCanBeCoercedToStrings() {
        assertThat(db.findUnique(String.class, "values (cast ('foo' as clob))"), is("foo"));
    }
}
