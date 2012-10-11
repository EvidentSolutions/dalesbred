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

public class DatabaseTransactionIsolationTest {

    private final Database db1 = TestDatabaseProvider.createTestDatabase();
    private final Database db2 = TestDatabaseProvider.createTestDatabase();

    @Test(expected = TransactionSerializationException.class)
    public void concurrentUpdatesInSerializableTransactionThrowTransactionSerializationException() {
        db1.setDefaultIsolation(SERIALIZABLE);
        db2.setDefaultIsolation(SERIALIZABLE);

        db1.update("drop table if exists isolation_test");
        db1.update("create table isolation_test (value int)");
        db1.update("insert into isolation_test (value) values (1)");

        db1.withTransaction(new TransactionCallback<Object>() {
            @Override
            public Object execute(@NotNull TransactionContext tx) throws SQLException {
                db1.findUniqueInt("select value from isolation_test");

                db2.withTransaction(new TransactionCallback<Object>() {
                    @Override
                    public Object execute(@NotNull TransactionContext tx) throws SQLException {
                        db2.findUniqueInt("select value from isolation_test");

                        db2.update("update isolation_test set value=2");
                        return null;
                    }
                });

                db1.update("update isolation_test set value=3");

                return null;
            }
        });
    }
}
