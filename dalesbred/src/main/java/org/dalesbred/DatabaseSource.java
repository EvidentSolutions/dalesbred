package org.dalesbred;

import org.dalesbred.conversion.TypeConversionRegistry;
import org.dalesbred.dialect.Dialect;
import org.dalesbred.internal.instantiation.InstantiatorProvider;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A factory for {@link DatabaseConnection} instances, sharing a single configuration
 * (dialect and type conversions) across all connections it creates.
 *
 * <p>Use {@link DatabaseSource} when you need explicit connection lifecycle management rather
 * than the automatic transaction handling provided by {@link Database}. Custom type conversions
 * registered via {@link #getTypeConversionRegistry()} apply to all connections opened by this source.
 *
 * <pre>{@code
 * var source = new DatabaseSource(dataSource, dialect);
 * source.getTypeConversionRegistry().registerConverter(...);
 *
 * try (DatabaseConnection conn = source.openConnection()) {
 *     conn.findAll(MyType.class, "select ...");
 * }
 * }</pre>
 *
 * @see DatabaseConnection
 * @see Database
 */
public final class DatabaseSource {

    private final @NotNull DataSource dataSource;
    private final @NotNull Dialect dialect;
    private final @NotNull InstantiatorProvider instantiatorRegistry;

    /**
     * Creates a new DatabaseSource using the given data source and dialect.
     */
    public DatabaseSource(@NotNull DataSource dataSource, @NotNull Dialect dialect) {
        this.dataSource = Objects.requireNonNull(dataSource);
        this.dialect = Objects.requireNonNull(dialect);
        this.instantiatorRegistry = new InstantiatorProvider(dialect);

        dialect.registerTypeConversions(instantiatorRegistry.getTypeConversionRegistry());
    }

    /**
     * Returns the type conversion registry, which can be used to register custom type conversions
     * that apply to all connections opened by this source.
     */
    public @NotNull TypeConversionRegistry getTypeConversionRegistry() {
        return instantiatorRegistry.getTypeConversionRegistry();
    }

    /**
     * Opens a new connection from the underlying data source with auto-commit disabled.
     * The caller is responsible for closing the connection, typically via try-with-resources.
     * Closing commits the transaction; see {@link DatabaseConnection#close()} for details.
     *
     * @throws DatabaseException if a connection cannot be obtained or configured
     */
    public @NotNull DatabaseConnection openConnection() {
        try {
            Connection connection = dataSource.getConnection();
            try {
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                try {
                    connection.close();
                } catch (SQLException e2) {
                    e.addSuppressed(e2);
                }
                throw e;
            }
            return new DatabaseConnection(connection, dialect, instantiatorRegistry);
        } catch (SQLException e) {
            throw new DatabaseSQLException(e);
        }
    }
}
