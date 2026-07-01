package org.dalesbred.integration.spring;

import org.dalesbred.Database;
import org.dalesbred.conversion.TypeConversionRegistry;
import org.dalesbred.dialect.Dialect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * Abstract base class for Spring Configuration to provide Dalesbred integration with Spring services.
 */
public abstract class DalesbredConfigurationSupport {

    @Bean
    public Database dalesbredDatabase(DataSource dataSource, PlatformTransactionManager transactionManager) {
        Dialect dialect = dialect();
        if (dialect == null)
            dialect = Dialect.detect(dataSource);

        Database db = new Database(new SpringTransactionManager(dataSource, transactionManager), dialect);
        registerTypeConversions(db.getTypeConversionRegistry());
        setupDatabase(db);
        return db;
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
    @SuppressWarnings("SameReturnValue")
    protected @Nullable Dialect dialect() {
        return null;
    }
}
