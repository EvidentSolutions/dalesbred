/*
 * Copyright (c) 2026 Evident Solutions Oy
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
