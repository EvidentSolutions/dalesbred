package org.dalesbred.dialect;

import org.dalesbred.conversion.TypeConversionPair;
import org.dalesbred.conversion.TypeConversionRegistry;
import org.dalesbred.internal.utils.EnumUtils;
import org.jetbrains.annotations.NotNull;
import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.function.Function;

/**
 * Support for PostgreSQL.
 */
public class PostgreSQLDialect extends Dialect {

    @Override
    public @NotNull <T extends Enum<T>, K> TypeConversionPair<Object,T> createNativeEnumConversions(@NotNull Class<T> enumType, @NotNull String typeName, @NotNull Function<T,K> keyFunction) {
        return new TypeConversionPair<>() {
            @Override
            public Object convertToDatabase(T obj) {
                return createPgObject(String.valueOf(keyFunction.apply(obj)), typeName);
            }

            @Override
            @SuppressWarnings("unchecked")
            public T convertFromDatabase(Object obj) {
                return EnumUtils.enumByKey(enumType, keyFunction, (K) obj);
            }
        };
    }

    private @NotNull Object createPgObject(@NotNull String value, @NotNull String typeName) {
        try {
            PGobject object = new PGobject();
            object.setType(typeName);
            object.setValue(value);
            return object;
        } catch (SQLException e) {
            throw convertException(e);
        }
    }

    @Override
    public void registerTypeConversions(@NotNull TypeConversionRegistry typeConversionRegistry) {
        typeConversionRegistry.registerConversionToDatabase(Date.class, v -> new Timestamp(v.getTime()));
    }
}
