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

package fi.evident.dalesbred.dialects;

import fi.evident.dalesbred.*;
import fi.evident.dalesbred.connection.ConnectionProvider;
import fi.evident.dalesbred.connection.DataSourceConnectionProvider;
import fi.evident.dalesbred.instantiation.TypeConversion;
import fi.evident.dalesbred.instantiation.TypeConversionRegistry;
import fi.evident.dalesbred.tx.TransactionManager;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Abstracts away the differences of databases.
 */
public abstract class Dialect {

    private static final String SERIALIZATION_FAILURE = "40001";

    private static final Logger log = Logger.getLogger(Dialect.class.getName());

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
    protected Object createDatabaseEnum(@NotNull Enum<?> value) {
        return value.name();
    }

    @NotNull
    public <T extends Enum<T>> TypeConversion<Object,T> getEnumCoercion(@NotNull final Class<T> enumType) {
        return new TypeConversion<Object, T>(Object.class, enumType) {
            @NotNull
            @Override
            public T convert(@NotNull Object value) {
                return Enum.valueOf(enumType, value.toString());
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
        return transactionManager.withTransaction(new TransactionSettings(), new TransactionCallback<Dialect>() {
            @Override
            public Dialect execute(@NotNull TransactionContext tx) throws SQLException {
                return detect(tx.getConnection());
            }
        }, new DefaultDialect());
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
    public static Dialect detect(@NotNull Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();

        if (productName.equals("PostgreSQL")) {
            log.fine("Automatically detected dialect PostgreSQL.");
            return new PostgreSQLDialect();

        } else if (productName.equals("HSQL Database Engine")) {
            log.fine("Automatically detected dialect HSQLDB.");
            return new HsqldbDialect();

        } else if (productName.equals("MySQL")) {
            log.fine("Automatically detected dialect MySQL.");
            return new MySQLDialect();

        } else if (productName.equals("Oracle")) {
            log.fine("Automatically detected dialect Oracle.");
            return new OracleDialect();

        } else {
            log.info("Could not detect dialect for product name '" + productName + "', falling back to default.");
            return new DefaultDialect();
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
}
