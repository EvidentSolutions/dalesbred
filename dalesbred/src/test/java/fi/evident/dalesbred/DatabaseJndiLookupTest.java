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

import fi.evident.dalesbred.support.MemoryContext;
import fi.evident.dalesbred.support.SystemPropertyRule;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Provider;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DatabaseJndiLookupTest {

    @Rule
    public final SystemPropertyRule initialFactoryRule = new SystemPropertyRule("java.naming.factory.initial", MyInitialFactory.class.getName());

    @Test
    public void createDatabaseByFetchingDataSourceFromJndi() throws NamingException {
        new InitialContext().bind("java:comp/env/foo", new MyDataSource(TestDatabaseProvider.createConnectionProvider()));

        Database db = Database.forJndiDataSource("java:comp/env/foo");
        assertThat(db.findUniqueInt("select 42"), is(42));
    }

    public static class MyInitialFactory implements InitialContextFactory {

        private static final MemoryContext initialContext = new MemoryContext();

        @Override
        public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
            return initialContext;
        }
    }

    private static class MyDataSource implements DataSource {

        private final Provider<Connection> connectionProvider;

        private MyDataSource(Provider<Connection> connectionProvider) {
            this.connectionProvider = connectionProvider;
        }

        @Override
        public Connection getConnection() throws SQLException {
            return connectionProvider.get();
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public PrintWriter getLogWriter() throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLogWriter(PrintWriter out) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getLoginTimeout() throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            throw new UnsupportedOperationException();
        }
    }
}
