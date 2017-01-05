/*
 * Copyright (c) 2015 Evident Solutions Oy
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

package org.dalesbred;

import org.dalesbred.testutils.LoggingController;
import org.dalesbred.testutils.SuppressLogging;
import org.junit.Rule;
import org.junit.Test;

import static org.dalesbred.transaction.Propagation.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class DatabaseTransactionPropagationTest {

    private final Database db = TestDatabaseProvider.createInMemoryHSQLDatabase();

    @Rule
    public final LoggingController loggingController = new LoggingController();

    @Test(expected = DatabaseException.class)
    public void mandatoryPropagationWithoutExistingTransactionThrowsException() {
        db.withTransaction(MANDATORY, tx -> null);
    }

    @Test
    public void mandatoryPropagationWithExistingTransactionProceedsNormally() {
        String result = db.withTransaction(REQUIRED, tx -> db.withTransaction(MANDATORY, tx1 -> "ok"));

        assertEquals("ok", result);
    }

    @Test
    @SuppressLogging
    public void nestedTransactions() {
        db.update("drop table if exists test_table");
        db.update("create table test_table (text varchar(64))");

        db.withVoidTransaction(tx -> {
            db.update("insert into test_table (text) values ('initial')");

            assertThat(db.findUnique(String.class, "select text from test_table"), is("initial"));

            try {
                db.withVoidTransaction(NESTED, tx1 -> {
                    db.update("update test_table set text = 'new-value'");

                    assertThat(db.findUnique(String.class, "select text from test_table"), is("new-value"));

                    throw new RuntimeException();
                });
                fail("did not receive expected exception");
            } catch (RuntimeException e) {
                // this is expected
            }

            assertThat(db.findUnique(String.class, "select text from test_table"), is("initial"));
        });
    }

    @Test
    public void requiresNewSuspendsActiveTransaction() {
        db.update("drop table if exists test_table");
        db.update("create table test_table (text varchar(64))");
        db.update("insert into test_table (text) values ('foo')");

        db.withVoidTransaction(tx -> {
            db.update("update test_table set text='bar'");

            db.withVoidTransaction(REQUIRES_NEW, tx1 ->
                    assertThat(db.findUnique(String.class, "select text from test_table"), is("foo")));

            assertThat(db.findUnique(String.class, "select text from test_table"), is("bar"));
        });
    }
}
