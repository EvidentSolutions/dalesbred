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

package org.dalesbred.dialects;

import org.dalesbred.*;
import org.dalesbred.connection.ConnectionProvider;
import org.dalesbred.connection.DataSourceConnectionProvider;
import org.dalesbred.instantiation.TypeConversion;
import org.dalesbred.instantiation.TypeConversionRegistry;
import org.dalesbred.tx.TransactionManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Abstracts away the differences of databases.
 */
public abstract class Dialect {

    private static final String SERIALIZATION_FAILURE = "40001";

    private static final Logger log = Logger.getLogger(Dialect.class.getName());

    @NotNull
    private EnumMode enumMode = EnumMode.NATIVE;

    @NotNull
    public Object valueToDatabase(@NotNull Object value) {
        if (value instanceof Enum<?>)
            return createDatabaseEnum((Enum<?>) value);
        else
            return value;
    }

    /**
     * Returns a database representation for given enum-value.
     */
    @NotNull
    private Object createDatabaseEnum(@NotNull Enum<?> value) {
        switch (enumMode) {
            case NAME:
                return value.name();
            case ORDINAL:
                return value.ordinal();
            case NATIVE:
                return createNativeDatabaseEnum(value);
        }

        throw new IllegalStateException("unknown enumMode: " + enumMode);
    }

    @NotNull
    private <T extends Enum<T>> T parseDatabaseEnum(@NotNull Class<T> enumType, @NotNull Object value) {
        switch (enumMode) {
            case NAME:
                return Enum.valueOf(enumType, value.toString());
            case ORDINAL:
                return enumByOrdinal(enumType, ((Number) value).intValue());
            case NATIVE:
                return parseNativeDatabaseEnum(enumType, value);
        }

        throw new IllegalStateException("unknown enumMode: " + enumMode);
    }

    @NotNull
    private static <T extends Enum<T>> T enumByOrdinal(@NotNull Class<T> enumType, int ordinal) {
        Enum<?>[] constants = enumType.getEnumConstants();
        if (ordinal >= 0 && ordinal < constants.length)
            return enumType.cast(constants[ordinal]);
        else
            throw new DatabaseException("invalid ordinal " + ordinal + " for enum type " + enumType.getName());
    }

    @NotNull
    protected Object createNativeDatabaseEnum(@NotNull Enum<?> value) {
        return value.name();
    }

    @NotNull
    protected <T extends Enum<T>> T parseNativeDatabaseEnum(@NotNull Class<T> enumType, @NotNull Object value) {
        return Enum.valueOf(enumType, value.toString());
    }

    @NotNull
    public <T extends Enum<T>> TypeConversion<Object, T> getEnumCoercion(@NotNull Class<T> enumType) {
        return new TypeConversion<Object, T>(Object.class, enumType) {
            @NotNull
            @Override
            public T convert(@NotNull Object value) {
                return parseDatabaseEnum(enumType, value);
            }

            @NotNull
            @Override
            public String toString() {
                return "EnumCoercion [" + enumType.getName() + ']';
            }
        };
    }

    @Override
    @NotNull
    public String toString() {
        return getClass().getName();
    }

    @NotNull
    public static Dialect detect(@NotNull DataSource dataSource) {
        return detect(new DataSourceConnectionProvider(dataSource));
    }

    @NotNull
    public static Dialect detect(@NotNull TransactionManager transactionManager) {
        return transactionManager.withTransaction(new TransactionSettings(),
                tx -> detect(tx.getConnection()), new DefaultDialect());
    }

    @NotNull
    public static Dialect detect(@NotNull ConnectionProvider connectionProvider) {
        try {
            Connection connection = connectionProvider.getConnection();
            try {
                return detect(connection);
            } finally {
                connectionProvider.releaseConnection(connection);
            }
        } catch (SQLException e) {
            throw new DatabaseSQLException("Failed to auto-detect database dialect: " + e, e);
        }
    }

    @NotNull
    public static Dialect detect(@NotNull Connection connection) {
        try {
            String productName = connection.getMetaData().getDatabaseProductName();

            switch (productName) {
                case "PostgreSQL":
                    log.fine("Automatically detected dialect PostgreSQL.");
                    return new PostgreSQLDialect();

                case "HSQL Database Engine":
                    log.fine("Automatically detected dialect HSQLDB.");
                    return new HsqldbDialect();

                case "MySQL":
                    log.fine("Automatically detected dialect MySQL.");
                    return new MySQLDialect();

                case "Oracle":
                    log.fine("Automatically detected dialect Oracle.");
                    return new OracleDialect();

                case "Microsoft SQL Server":
                    log.fine("Automatically detected dialect SQLServer.");
                    return new SQLServerDialect();

                default:
                    log.info("Could not detect dialect for product name '" + productName + "', falling back to default.");
                    return new DefaultDialect();
            }
        } catch (SQLException e) {
            throw new DatabaseSQLException("Failed to auto-detect database dialect: " + e, e);
        }
    }

    @NotNull
    public DatabaseException convertException(@NotNull SQLException e) {
        String sqlState = e.getSQLState();
        if (sqlState == null)
            return new DatabaseSQLException(e);

        if (sqlState.equals(SERIALIZATION_FAILURE))
            return new TransactionSerializationException(e);
        else if (sqlState.startsWith("40"))
            return new TransactionRollbackException(e);
        else
            return new DatabaseSQLException(e);
    }

    public void registerTypeConversions(@NotNull TypeConversionRegistry typeConversionRegistry) {

    }

    /**
     * Sets the way enumerations are persisted.
     */
    public void setEnumMode(@NotNull EnumMode enumMode) {
        this.enumMode = enumMode;
    }

    /**
     * Gets the way enumerations are persisted.
     */
    @NotNull
    public EnumMode getEnumMode() {
        return enumMode;
    }

    /**
     * Bind object to {@link PreparedStatement}. Can be overridden by subclasses to
     * implement custom argument binding.
     *
     * @param ps statement to bind object to
     * @param index index of the parameter
     * @param value to bind
     * @throws SQLException if something fails
     */
    public void bindArgument(@NotNull PreparedStatement ps, int index, @Nullable Object value) throws SQLException {
        ArgumentBinder.bindArgument(ps, index, value);
    }
}
