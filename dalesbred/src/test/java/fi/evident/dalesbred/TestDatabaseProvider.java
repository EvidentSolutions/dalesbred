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

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.name.Names;
import fi.evident.dalesbred.connection.DriverManagerDataSourceProvider;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assume.assumeNotNull;

public final class TestDatabaseProvider {

    private TestDatabaseProvider() { }

    @NotNull
    public static Database createInMemoryHSQLDatabase() {
        return Database.forUrlAndCredentials("jdbc:hsqldb:mem:test;hsqldb.tx=mvcc", "sa", "");
    }

    @NotNull
    public static Database createPostgreSQLDatabase() {
        return new Database(createDataSource("postgresql-connection.properties"));
    }

    @NotNull
    public static Database createMySQLDatabase() {
        return new Database(createDataSource("mysql-connection.properties"));
    }

    @NotNull
    public static DataSource createInMemoryHSQLDataSource() {
        return DriverManagerDataSourceProvider.createDataSource("jdbc:hsqldb:.", "sa", "");
    }

    @NotNull
    private static DataSource createDataSource(@NotNull String name) {
        Properties props = loadProperties(name);
        String url = props.getProperty("jdbc.url");
        String login = props.getProperty("jdbc.login");
        String password = props.getProperty("jdbc.password");

        return DriverManagerDataSourceProvider.createDataSource(url, login, password);
    }

    @NotNull
    public static Module inMemoryDatabasePropertiesModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                Properties props = new Properties();
                props.setProperty("jdbc.url", "jdbc:hsqldb:.");
                props.setProperty("jdbc.login", "sa");
                props.setProperty("jdbc.password", "");

                Names.bindProperties(binder(), props);
            }
        };
    }

    @NotNull
    @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
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
}
