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
import org.dalesbred.transaction.TransactionSerializationException;
import org.dalesbred.transaction.TransactionSettings;
import org.junit.Rule;
import org.junit.Test;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DatabaseTransactionRetryTest {

    private final Database db = TestDatabaseProvider.createInMemoryHSQLDatabase();

    @Rule
    public final LoggingController loggingController = new LoggingController();

    @Test
    @SuppressLogging
    public void maxRetries() {
        AtomicInteger tries = new AtomicInteger(0);

        TransactionSettings settings = new TransactionSettings();
        settings.setRetries(3);

        try {
            db.withVoidTransaction(settings, tx -> {
                tries.incrementAndGet();
                throw new TransactionSerializationException(new SQLException());
            });
        } catch (TransactionSerializationException e) {
            // expected
        }

        assertThat(tries.get(), is(4));
    }
}
