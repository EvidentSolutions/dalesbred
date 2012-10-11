package fi.evident.dalesbred;

import fi.evident.dalesbred.dialects.DefaultDialect;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DatabaseCustomDialectTest {

    private final Database db = TestDatabaseProvider.createTestDatabase(new UppercaseDialect());

    @Rule
    public final TransactionalTestsRule rule = new TransactionalTestsRule(db);

    @Test
    public void customDialect() {
        db.update("drop table if exists my_table");
        db.update("create table my_table (text varchar)");

        db.update("insert into my_table values (?)", "foo");

        assertEquals("FOO", db.findUnique(String.class, "select text from my_table"));
    }

    private static final class UppercaseDialect extends DefaultDialect {
        @NotNull
        @Override
        public Object valueToDatabase(@NotNull Object value) {
            return value.toString().toUpperCase();
        }
    }
}
