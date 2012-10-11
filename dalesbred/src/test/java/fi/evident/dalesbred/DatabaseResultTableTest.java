package fi.evident.dalesbred;

import org.junit.Rule;
import org.junit.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class DatabaseResultTableTest {

    private final Database db = TestDatabaseProvider.createTestDatabase();

    @Rule
    public final TransactionalTestsRule rule = new TransactionalTestsRule(db);

    @Test
    public void fetchSimpleResultTable() {
        ResultTable table = db.findTable("select 42 as num, 'foo' as str, true as bool");

        assertThat(table.getColumnCount(), is(3));
        assertThat(table.getColumnNames(), is(asList("num", "str", "bool")));
        assertThat(table.getColumnTypes(), is(types(Integer.class, String.class, Boolean.class)));
        assertThat(table.getColumns().toString(), is("[num: java.lang.Integer, str: java.lang.String, bool: java.lang.Boolean]"));
        assertThat(table.getColumns().get(1).getIndex(), is(1));

        assertThat("jdbcType[0]", table.getColumns().get(0).getJdbcType(), is(Types.INTEGER));
        assertThat("databaseType[0]", table.getColumns().get(0).getDatabaseType(), is("int4"));

        assertThat(table.getRowCount(), is(1));
        assertThat(table.getRows().get(0).asList(), is(values(42, "foo", true)));
        assertEquals("foo", table.get(0, 1));
        assertEquals("foo", table.get(0, "str"));

        assertThat(table.toString(), is("ResultTable [columns=[num: java.lang.Integer, str: java.lang.String, bool: java.lang.Boolean], rows=1]"));
    }

    private static List<Object> values(Object... values) {
        return Arrays.asList(values);
    }

    private static List<Class<?>> types(Class<?>... classes) {
        return Arrays.asList(classes);
    }
}
