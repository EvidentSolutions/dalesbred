package fi.evident.dalesbred;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static fi.evident.dalesbred.Isolation.SERIALIZABLE;
import static fi.evident.dalesbred.Propagation.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class DatabaseTransactionPropagationTest {

    private final Database db = TestDatabaseProvider.createTestDatabase();

    @Test(expected = DatabaseException.class)
    public void mandatoryPropagationWithoutExistingTransactionThrowsException() {
        db.withTransaction(MANDATORY, new TransactionCallback<Object>() {
            @Override
            public Object execute(@NotNull Connection connection) throws SQLException {
                return null;
            }
        });
    }

    @Test
    public void mandatoryPropagationWithExistingTransactionProceedsNormally() {
        db.withTransaction(REQUIRED, new TransactionCallback<Object>() {
            @Override
            public Object execute(@NotNull Connection connection) throws SQLException {
                return db.withTransaction(MANDATORY, new TransactionCallback<Object>() {
                    @Override
                    public Object execute(@NotNull Connection connection) throws SQLException {
                        return null;
                    }
                });
            }
        });
    }

    @Test
    public void nestedTransactions() {
        db.update("drop table if exists test_table");
        db.update("create table test_table (text varchar)");

        db.withTransaction(new TransactionCallback<Object>() {
            @Override
            public Object execute(@NotNull Connection connection) throws SQLException {
                db.update("insert into test_table values ('initial')");

                assertThat(db.findUnique(String.class, "select text from test_table"), is("initial"));

                try {
                    db.withTransaction(NESTED, new TransactionCallback<Object>() {
                        @Override
                        public Object execute(@NotNull Connection connection) throws SQLException {
                            db.update("update test_table set text = 'new-value'");

                            assertThat(db.findUnique(String.class, "select text from test_table"), is("new-value"));

                            throw new RuntimeException();
                        }
                    });
                    fail("did not receive expected exception");
                } catch (RuntimeException e) {
                    // this is expected
                }

                assertThat(db.findUnique(String.class, "select text from test_table"), is("initial"));

                return null;
            }
        });
    }

    @Test
    public void requiresNewSuspendsActiveTransaction() {
        db.setDefaultIsolation(SERIALIZABLE);

        db.update("drop table if exists test_table");
        db.update("create table test_table (text varchar)");
        db.update("insert into test_table values ('foo')");

        db.withTransaction(new TransactionCallback<Object>() {
            @Override
            public Object execute(@NotNull Connection connection) throws SQLException {
                db.update("update test_table set text='bar'");

                db.withTransaction(REQUIRES_NEW, new TransactionCallback<Object>() {
                    @Override
                    public Object execute(@NotNull Connection connection) throws SQLException {
                        assertThat(db.findUnique(String.class, "select text from test_table"), is("foo"));
                        return null;
                    }
                });

                assertThat(db.findUnique(String.class, "select text from test_table"), is("bar"));
                return null;
            }
        });
    }
}
