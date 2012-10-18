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

package fi.evident.dalesbred.support.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import fi.evident.dalesbred.Database;
import fi.evident.dalesbred.connection.DataSourceConnectionProvider;

import javax.inject.Singleton;
import java.sql.Connection;

import static fi.evident.dalesbred.support.guice.GuiceSupport.bindTransactionInterceptor;
import static fi.evident.dalesbred.utils.Require.requireNonNull;

/**
 * A Guice module for configuring the database using a {@link javax.sql.DataSource}.
 * Assumes that a DataSource has been bound to the context.
 *
 * @see DriverManagerDatabaseModule
 */
public final class DataSourceDatabaseModule extends AbstractModule {

    private final Key<Database> databaseKey;

    /**
     * Creates a module that creates a default database instance.
     */
    public DataSourceDatabaseModule() {
        this(Key.get(Database.class));
    }

    /**
     * Creates a module that creates a database instance with given key.
     */
    public DataSourceDatabaseModule(Key<Database> databaseKey) {
        this.databaseKey = requireNonNull(databaseKey);
    }

    @Override
    protected void configure() {
        bind(Connection.class).toProvider(DataSourceConnectionProvider.class);

        if (databaseKey.hasAttributes())
            bind(databaseKey).to(Database.class).in(Singleton.class);
        else
            bind(databaseKey).in(Singleton.class);

        bindTransactionInterceptor(binder(), databaseKey);
    }
}
