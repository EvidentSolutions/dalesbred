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

import org.dalesbred.support.MemoryContext;
import org.dalesbred.support.SystemPropertyRule;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.spi.InitialContextFactory;
import java.util.Hashtable;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DatabaseJndiLookupTest {

    @Rule
    public final SystemPropertyRule initialFactoryRule = new SystemPropertyRule("java.naming.factory.initial", MyInitialFactory.class.getName());

    @Test
    public void createDatabaseByFetchingDataSourceFromJndi() {
        MyInitialFactory.initialContext.bind("java:comp/env/foo", TestDatabaseProvider.createInMemoryHSQLDataSource());

        Database db = Database.forJndiDataSource("java:comp/env/foo");
        assertThat(db.findUniqueInt("values (42)"), is(42));
    }

    public static class MyInitialFactory implements InitialContextFactory {

        private static final MemoryContext initialContext = new MemoryContext();

        @NotNull
        @Override
        public Context getInitialContext(Hashtable<?, ?> environment) {
            return initialContext;
        }
    }
}
