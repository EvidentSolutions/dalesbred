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

import org.dalesbred.connection.ConnectionProvider;
import org.dalesbred.connection.DriverManagerConnectionProvider;
import org.dalesbred.transaction.TransactionCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.Assume.assumeNotNull;

public final class TestDatabaseProvider {

    private TestDatabaseProvider() { }

    @NotNull
    public static Database createInMemoryHSQLDatabase() {
        return Database.forUrlAndCredentials("jdbc:hsqldb:mem:test;hsqldb.tx=mvcc", "sa", "");
    }

    @NotNull
    public static Database createPostgreSQLDatabase() {
        return new Database(createConnectionProvider("postgresql-connection.properties"));
    }

    @NotNull
    public static ConnectionProvider createMySQLConnectionProvider() {
        return createConnectionProvider("mysql-connection.properties");
    }

    @NotNull
    public static ConnectionProvider createInMemoryHSQLConnectionProvider() {
        return new DriverManagerConnectionProvider("jdbc:hsqldb:.", "sa", "");
    }

    @NotNull
    public static DataSource createInMemoryHSQLDataSource() {
        return new DriverManagerDataSource("jdbc:hsqldb:.", "sa", "");
    }

    @NotNull
    private static ConnectionProvider createConnectionProvider(@NotNull String name) {
        Properties props = loadProperties(name);
        String url = props.getProperty("jdbc.url");
        String login = props.getProperty("jdbc.login");
        String password = props.getProperty("jdbc.password");

        return new DriverManagerConnectionProvider(url, login, password);
    }

    @NotNull
    @SuppressWarnings({"IOResourceOpenedButNotSafelyClosed", "ThrowFromFinallyBlock"})
    private static Properties loadProperties(@NotNull String name) {
        try {
            InputStream in = TransactionCallback.class.getClassLoader().getResourceAsStream(name);
            assumeNotNull(in);
            try {
                Properties properties = new Properties();
                properties.load(in);
                return properties;
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final class DriverManagerDataSource implements DataSource {

        @NotNull
        private final String url;

        @Nullable
        private final String defaultUser;

        @Nullable
        private final String defaultPassword;

        private DriverManagerDataSource(@NotNull String url, @Nullable String defaultUser, @Nullable String defaultPassword) {
            this.url = url;
            this.defaultUser = defaultUser;
            this.defaultPassword = defaultPassword;
        }

        @Override
        public Connection getConnection() throws SQLException {
            return getConnection(defaultUser, defaultPassword);
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return DriverManager.getConnection(url, username, password);
        }

        @Override
        public PrintWriter getLogWriter() throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        public void setLogWriter(PrintWriter out) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        public int getLoginTimeout() throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }
    }
}
