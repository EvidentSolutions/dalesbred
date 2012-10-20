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
import fi.evident.dalesbred.connection.DriverManagerConnectionProvider;
import fi.evident.dalesbred.dialects.Dialect;
import org.jetbrains.annotations.NotNull;

import javax.inject.Provider;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.Properties;

import static org.junit.Assume.assumeNotNull;

public final class TestDatabaseProvider {

    private TestDatabaseProvider() { }

    @NotNull
    public static Database createTestDatabase() {
        return new Database(createConnectionProvider());
    }

    @NotNull
    public static Database createTestDatabase(@NotNull Dialect dialect) {
        return new Database(createConnectionProvider(), dialect);
    }

    @NotNull
    public static Provider<Connection> createConnectionProvider() {
        Properties props = loadProperties();
        String url = props.getProperty("jdbc.url");
        String login = props.getProperty("jdbc.login");
        String password = props.getProperty("jdbc.password");

        return new DriverManagerConnectionProvider(url, login, password);
    }

    @NotNull
    public static DataSource createDataSource() {
        return createDataSource(createConnectionProvider());
    }

    @NotNull
    public static Module propertiesModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                Properties props = loadProperties();

                Names.bindProperties(binder(), props);
            }
        };
    }

    @NotNull
    public static Properties loadProperties() {
        try {
            InputStream in = TransactionCallback.class.getClassLoader().getResourceAsStream("connection.properties");
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


    /**
     * Creates a simple DataSource that can only return connections from the provider. This is implemented
     * reflectively because new versions of JDK have added new methods to DataSource and we want to be able
     * to run the test on all versions.
     */
    @NotNull
    private static DataSource createDataSource(@NotNull final Provider<Connection> connection) {
        return (DataSource) Proxy.newProxyInstance(DatabaseJndiLookupTest.class.getClassLoader(), new Class<?>[]{DataSource.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, @NotNull Method method, Object[] args) throws Throwable {
                if (method.getName().equals("getConnection"))
                    return connection.get();
                else
                    throw new UnsupportedOperationException("unsupported operation: " + method);
            }
        });
    }
}
