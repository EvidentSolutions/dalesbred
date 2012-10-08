package fi.evident.dalesbred.dialects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Abstracts away the differences of databases.
 */
public abstract class Dialect {

    private static final Logger log = Logger.getLogger(Dialect.class.getName());

    @Nullable
    public Object valueToDatabase(@Nullable Object value) {
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
    public <T extends Enum<T>> T databaseValueToEnum(@NotNull Class<T> type, @NotNull Object value) {
        return Enum.valueOf(type, value.toString());
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
