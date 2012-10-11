package fi.evident.dalesbred.dialects;

import fi.evident.dalesbred.DatabaseException;
import fi.evident.dalesbred.instantiation.Coercion;
import fi.evident.dalesbred.instantiation.CoercionBase;
import org.jetbrains.annotations.NotNull;

import javax.inject.Provider;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Abstracts away the differences of databases.
 */
public abstract class Dialect {

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
    public <T extends Enum<T>> Coercion<Object,T> getEnumCoercion(@NotNull final Class<T> enumType) {
        return new CoercionBase<Object, T>(Object.class, enumType) {
            @NotNull
            @Override
            public T coerce(@NotNull Object value) {
                return Enum.valueOf(enumType, value.toString());
            }

            @Override
            public String toString() {
                return "EnumCoercion [" + enumType.getName() + "]";
            }
        };
    }

    @Override
    @NotNull
    public String toString() {
        return getClass().getName();
    }

    @NotNull
    public static Dialect detect(@NotNull Provider<Connection> connectionProvider) {
        try {
            Connection connection = connectionProvider.get();
            try {
                return Dialect.detect(connection);
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed autodetect database dialect: " + e, e);
        }
    }

    @NotNull
    public static Dialect detect(@NotNull Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();

        if (productName.equals("PostgreSQL")) {
            log.fine("automatically detected dialect PostgreSQL");
            return new PostgreSQLDialect();
        } else {
            log.info("Could not detect dialect for product name '" + productName + "', falling back to default.");
            return new DefaultDialect();
        }
    }
}
