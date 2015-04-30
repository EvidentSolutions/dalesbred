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

package org.dalesbred.integration.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;
import org.dalesbred.Database;
import org.dalesbred.connection.DriverManagerDataSourceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.sql.DataSource;

import static java.util.Objects.requireNonNull;

/**
 * A Guice module for configuring the database using DriverManager. Useful for testing,
 * but not much else.
 * <p>
 * Assumes that following named strings are bound:
 *
 * <table>
 *     <tr><td>jdbc.url</td><td>JDBC URL of the database to connect</td></tr>
 *     <tr><td>jdbc.login</td><td>login of the user</td></tr>
 *     <tr><td>jdbc.password</td><td>password of the user</td></tr>
 * </table>
 *
 * @see DataSourceDatabaseModule
 */
public final class DriverManagerDatabaseModule extends AbstractModule {

    @NotNull
    private final Key<Database> databaseKey;

    /**
     * Creates a module that creates a default database instance.
     */
    public DriverManagerDatabaseModule() {
        this(Key.get(Database.class));
    }

    /**
     * Creates a module that creates a database instance with given key.
     */
    public DriverManagerDatabaseModule(@NotNull Key<Database> databaseKey) {
        this.databaseKey = requireNonNull(databaseKey);
    }

    @Override
    protected void configure() {
        bind(databaseKey).toProvider(DatabaseProvider.class).in(Singleton.class);

        GuiceSupport.bindTransactionInterceptor(binder(), databaseKey);
    }

    @Provides @Singleton
    DataSource dataSourceFromParameters(@NotNull @Named("jdbc.url") String url,
                                        @Nullable @Named("jdbc.login") String user,
                                        @Nullable @Named("jdbc.password") String password) {
        return DriverManagerDataSourceProvider.createDataSource(url, user, password);
    }
}
