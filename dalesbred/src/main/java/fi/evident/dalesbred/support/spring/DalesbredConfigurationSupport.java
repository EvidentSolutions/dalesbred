/*
 * Copyright (c) 2013 Evident Solutions Oy
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
package fi.evident.dalesbred.support.spring;

import fi.evident.dalesbred.Database;
import fi.evident.dalesbred.dialects.Dialect;
import fi.evident.dalesbred.instantiation.InstantiatorRegistry;
import fi.evident.dalesbred.instantiation.TypeConversionRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * Abstract base class for Spring Configuration to provide Dalesbred integration with Spring services.
 */
public abstract class DalesbredConfigurationSupport {

    @Bean
    @Autowired
    public Database dalesbredDatabase(DataSource dataSource, PlatformTransactionManager transactionManager) {
        Dialect dialect = dialect();
        if (dialect == null)
            dialect = Dialect.detect(dataSource);

        Database db = new Database(new SpringTransactionManager(dataSource, transactionManager), dialect);
        registerInstantiators(db.getInstantiatorRegistry());
        registerTypeConversions(db.getTypeConversionRegistry());
        setupDatabase(db);
        return db;
    }

    /**
     * Can be overridden by subclasses to register custom instantiators.
     */
    @SuppressWarnings({"UnusedParameters", "EmptyMethod"})
    protected void registerInstantiators(@NotNull InstantiatorRegistry registry) {
    }

    /**
     * Can be overridden by subclasses to register custom type conversions.
     */
    @SuppressWarnings({"UnusedParameters", "EmptyMethod"})
    protected void registerTypeConversions(@NotNull TypeConversionRegistry registry) {
    }

    /**
     * Can be overridden by subclasses to perform custom database setup.
     */
    @SuppressWarnings({"UnusedParameters", "EmptyMethod"})
    protected void setupDatabase(@NotNull Database db) {
    }

    /**
     * Subclasses can override this to return the {@link Dialect} to use. By default
     * {@code null} is returned, which means that dialect is auto-detected.
     */
    @Nullable
    protected Dialect dialect() {
        return null;
    }
}
