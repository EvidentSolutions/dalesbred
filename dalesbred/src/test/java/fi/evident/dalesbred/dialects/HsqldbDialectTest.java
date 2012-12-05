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

package fi.evident.dalesbred.dialects;

import fi.evident.dalesbred.Database;
import fi.evident.dalesbred.TransactionCallback;
import fi.evident.dalesbred.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HsqldbDialectTest {

    private final Database db = Database.forUrlAndCredentials("jdbc:hsqldb:.", "sa", "");

    @Test
    public void simpleQuery() {
        assertThat(db.findUniqueInt("VALUES (42)"), is(42));
    }

    @Test
    public void detectDialect() {
        db.withTransaction(new TransactionCallback<Void>() {
            @Override
            @Nullable
            public Void execute(@NotNull TransactionContext tx) throws SQLException {
                Dialect dialect = Dialect.detect(tx.getConnection());

                assertThat(dialect, is(HsqldbDialect.class));
                return null;
            }
        });
    }
}
