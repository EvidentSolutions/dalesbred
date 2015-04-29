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

package org.dalesbred.tx;

import org.dalesbred.Database;
import org.dalesbred.TestDatabaseProvider;
import org.dalesbred.TransactionCallback;
import org.dalesbred.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class SingleConnectionTransactionManagerTest {

    private final DataSource dataSource = TestDatabaseProvider.createInMemoryHSQLDataSource();

    @Test
    public void rollbacksWithoutThirdPartyTransactions() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            TransactionManager tm = new SingleConnectionTransactionManager(connection, false);
            final Database db = new Database(tm);

            db.update("drop table if exists test_table");
            db.update("create table test_table (text varchar(64))");
            db.update("insert into test_table (text) values ('foo')");

            db.withTransaction(new TransactionCallback<Object>() {
                @Nullable
                @Override
                public Object execute(@NotNull TransactionContext tx) {
                    db.update("update test_table set text='bar'");
                    tx.setRollbackOnly();
                    return null;
                }
            });

            assertThat(db.findUnique(String.class, "select text from test_table"), is("foo"));
        }
    }

    @Test
    public void verifyHasTransaction() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Database db1 = new Database(new SingleConnectionTransactionManager(connection, true));
            assertTrue(db1.hasActiveTransaction());

            Database db2 = new Database(new SingleConnectionTransactionManager(connection, false));
            assertFalse(db2.hasActiveTransaction());
        }
    }
}
