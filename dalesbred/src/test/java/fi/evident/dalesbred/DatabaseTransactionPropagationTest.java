/*
 * Copyright (c) 2012 Evident Solutions Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fi.evident.dalesbred;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

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
            public Object execute(@NotNull TransactionContext tx) throws SQLException {
                return null;
            }
        });
    }

    @Test
    public void mandatoryPropagationWithExistingTransactionProceedsNormally() {
        db.withTransaction(REQUIRED, new TransactionCallback<Object>() {
            @Override
            public Object execute(@NotNull TransactionContext tx) throws SQLException {
                return db.withTransaction(MANDATORY, new TransactionCallback<Object>() {
                    @Override
                    public Object execute(@NotNull TransactionContext tx) throws SQLException {
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
            public Object execute(@NotNull TransactionContext tx) throws SQLException {
                db.update("insert into test_table values ('initial')");

                assertThat(db.findUnique(String.class, "select text from test_table"), is("initial"));

                try {
                    db.withTransaction(NESTED, new TransactionCallback<Object>() {
                        @Override
                        public Object execute(@NotNull TransactionContext tx) throws SQLException {
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
            public Object execute(@NotNull TransactionContext tx) throws SQLException {
                db.update("update test_table set text='bar'");

                db.withTransaction(REQUIRES_NEW, new TransactionCallback<Object>() {
                    @Override
                    public Object execute(@NotNull TransactionContext tx) throws SQLException {
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
