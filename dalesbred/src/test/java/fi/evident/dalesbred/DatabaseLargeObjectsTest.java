package fi.evident.dalesbred;

import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;

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

    @Test
    @SuppressWarnings("PrimitiveArrayArgumentToVariableArgMethod")
    public void blobColumnsCanBeCoercedToStrings() {
        byte[] data = { 1, 2, 3 };
        assertThat(db.findUnique(byte[].class, "values (cast (? as blob))", data), is(data));
    }

    @Test
    public void streamClobToDatabase() throws Exception {
        db.update("drop table if exists clob_test");
        db.update("create temporary table clob_test (id int, clob_data clob)");

        String originalData = "foobar";
        db.update("insert into clob_test values (1, ?)", new StringReader(originalData));

        String data = db.findUnique(String.class, "select clob_data from clob_test where id=1");
        assertThat(data, is(originalData));
    }

    @Test
    public void streamBlobToDatabase() throws Exception {
        db.update("drop table if exists blob_test");
        db.update("create temporary table blob_test (id int, blob_data blob)");

        byte[] originalData = { 25, 35, 3 };
        db.update("insert into blob_test values (1, ?)", new ByteArrayInputStream(originalData));

        byte[] data = db.findUnique(byte[].class, "select blob_data from blob_test where id=1");
        assertThat(data, is(originalData));
    }
}
