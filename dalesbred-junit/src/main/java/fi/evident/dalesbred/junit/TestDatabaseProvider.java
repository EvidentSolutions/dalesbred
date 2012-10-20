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

package fi.evident.dalesbred.junit;

import fi.evident.dalesbred.Database;
import fi.evident.dalesbred.TransactionCallback;
import fi.evident.dalesbred.connection.DriverManagerConnectionProvider;
import fi.evident.dalesbred.dialects.Dialect;
import org.jetbrains.annotations.NotNull;
import org.junit.internal.AssumptionViolatedException;

import javax.inject.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;

/**
 * <p>
 *   Helper methods for creating databases using a properties-file in classpath.
 * </p>
 * <p>
 *   The properties should have the following keys:
 * </p>
 * <table
 *  <tr><td>jdbc.url</td><td>required</td><td>JDBC url of the database to connect</td></tr>
 *  <tr><td>jdbc.login</td><td>optional</td><td>login for the database</td></th></tr>
 *  <tr><td>jdbc.password</td><td>optional</td><td>password for the database</td></th></tr>
 * </table>
 */
public final class TestDatabaseProvider {

    private TestDatabaseProvider() { }

    /**
     * Loads database with given properties.
     *
     * @throws org.junit.internal.AssumptionViolatedException if properties were not found
     */
    @NotNull
    public static Database databaseForProperties(@NotNull String propertiesPath) {
        return new Database(createConnectionProvider(propertiesPath));
    }

    /**
     * Loads database with given properties, using specified dialect.
     *
     * @throws org.junit.internal.AssumptionViolatedException if properties were not found
     */
    @NotNull
    public static Database databaseForProperties(@NotNull String propertiesPath, @NotNull Dialect dialect) {
        return new Database(createConnectionProvider(propertiesPath), dialect);
    }

    @NotNull
    private static Provider<Connection> createConnectionProvider(@NotNull String propertiesPath) {
        Properties props = loadConnectionProperties(propertiesPath);

        String url = props.getProperty("jdbc.url");
        if (url == null)
            throw new RuntimeException("Could not find 'jdbc.url' in '" + propertiesPath + "'.");

        String login = props.getProperty("jdbc.login");
        String password = props.getProperty("jdbc.password");

        return new DriverManagerConnectionProvider(url, login, password);
    }

    @NotNull
    private static Properties loadConnectionProperties(@NotNull String propertiesName) {
        InputStream in = TransactionCallback.class.getClassLoader().getResourceAsStream(propertiesName);
        if (in == null)
            throw new AssumptionViolatedException("Could not find database configuration file '" + propertiesName + "'.");
        try {
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
