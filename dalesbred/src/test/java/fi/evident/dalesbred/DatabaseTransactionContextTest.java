package fi.evident.dalesbred;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DatabaseTransactionContextTest {
    private final Database db = TestDatabaseProvider.createTestDatabase();

    @Test
    public void rollbackOnly() {
        db.update("drop table if exists test_table");
        db.update("create table test_table (text varchar)");
        db.update("insert into test_table values ('foo')");

        db.withTransaction(new TransactionCallback<Object>() {
            @Override
            public Object execute(@NotNull TransactionContext tx) throws SQLException {
                db.update("update test_table set text='bar'");
                tx.setRollbackOnly();
                return null;
            }
        });

        assertThat(db.findUnique(String.class, "select text from test_table"), is("foo"));
    }
}
