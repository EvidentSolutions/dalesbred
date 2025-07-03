/*
 * Copyright (c) 2017 Evident Solutions Oy
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

package org.dalesbred.dialect;

import org.dalesbred.DatabaseException;
import org.dalesbred.DatabaseSQLException;
import org.dalesbred.connection.ConnectionProvider;
import org.dalesbred.connection.DataSourceConnectionProvider;
import org.dalesbred.conversion.TypeConversionPair;
import org.dalesbred.conversion.TypeConversionRegistry;
import org.dalesbred.internal.jdbc.ArgumentBinder;
import org.dalesbred.transaction.TransactionManager;
import org.dalesbred.transaction.TransactionRollbackException;
import org.dalesbred.transaction.TransactionSerializationException;
import org.dalesbred.transaction.TransactionSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Abstracts away the differences of databases.
 */
public abstract class Dialect {

    private static final String SERIALIZATION_FAILURE = "40001";

    private static final Logger log = LoggerFactory.getLogger(Dialect.class);

    protected final Map<String, Class<?>> resultSetMetaDataTypeOverrides = new ConcurrentHashMap<>();

    public @NotNull Object valueToDatabase(@NotNull Object value) {
        return value;
    }

    public @NotNull <T extends Enum<T>, K> TypeConversionPair<Object,T> createNativeEnumConversions(@NotNull Class<T> enumType, @NotNull String typeName, @NotNull Function<T,K> keyFunction) {
        throw new UnsupportedOperationException("native enums are not supported by " + getClass().getName());
    }

    @Override
    public @NotNull String toString() {
        return getClass().getName();
    }

    public static @NotNull Dialect detect(@NotNull DataSource dataSource) {
        return detect(new DataSourceConnectionProvider(dataSource));
    }

    public static @NotNull Dialect detect(@NotNull TransactionManager transactionManager) {
        return transactionManager.withTransaction(new TransactionSettings(),
                tx -> detect(tx.getConnection()), new DefaultDialect());
    }

    public static @NotNull Dialect detect(@NotNull ConnectionProvider connectionProvider) {
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

    public static @NotNull Dialect detect(@NotNull Connection connection) {
        try {
            String productName = connection.getMetaData().getDatabaseProductName();

            switch (productName) {
                case "PostgreSQL":
                    log.debug("Automatically detected dialect PostgreSQL.");
                    return new PostgreSQLDialect();

                case "HSQL Database Engine":
                    log.debug("Automatically detected dialect HSQLDB.");
                    return new HsqldbDialect();

                case "H2":
                    log.debug("Automatically detected dialect H2.");
                    return new H2Dialect();

                case "MySQL":
                    log.debug("Automatically detected dialect MySQL.");
                    return new MySQLDialect();

                case "MariaDB":
                    log.debug("Automatically detected dialect MariaDB.");
                    return new MariaDBDialect(connection.getMetaData().getDriverVersion());

                case "Oracle":
                    log.debug("Automatically detected dialect Oracle.");
                    return new OracleDialect();

                case "Microsoft SQL Server":
                    log.debug("Automatically detected dialect SQLServer.");
                    return new SQLServerDialect();

                default:
                    log.info("Could not detect dialect for product name '{}', falling back to default.", productName);
                    return new DefaultDialect();
            }
        } catch (SQLException e) {
            throw new DatabaseSQLException("Failed to auto-detect database dialect: " + e, e);
        }
    }

    public @NotNull DatabaseException convertException(@NotNull SQLException e) {
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
     * Bind object to {@link PreparedStatement}. Can be overridden by subclasses to
     * implement custom argument binding.
     *
     * @param ps    statement to bind object to
     * @param index index of the parameter
     * @param value to bind
     * @throws SQLException if something fails
     */
    public void bindArgument(@NotNull PreparedStatement ps, int index, @Nullable Object value) throws SQLException {
        ArgumentBinder.bindArgument(ps, index, value);
    }

    public Map<String, Class<?>> getResultSetMetaDataTypeOverrides() {
        return resultSetMetaDataTypeOverrides;
    }

    // Some drivers (e.g. MySQL and H2) return version strings that do not look like typical SemVer strings and hence will not work with this
    protected static class Version implements Comparable<Version> {

        private final int[] numbers;

        private Version(@NotNull String version) {
            final String split[] = version.split("\\-")[0].split("\\.");
            numbers = new int[split.length];
            for (int i = 0; i < split.length; i++)
                numbers[i] = Integer.parseInt(split[i]);
        }

        protected static Version withNumber(@NotNull String version) {
            return new Version(version);
        }

        @Override
        public int compareTo(@NotNull Version another) {
            final int maxLength = Math.max(numbers.length, another.numbers.length);
            for (int i = 0; i < maxLength; i++) {
                final int left = i < numbers.length ? numbers[i] : 0;
                final int right = i < another.numbers.length ? another.numbers[i] : 0;
                if (left != right)
                    return left < right ? -1 : 1;
            }
            return 0;
        }

        protected boolean isGreaterThanOrEqualTo(Version another) {
            return this.compareTo(another) >= 0;
        }
    }
}
