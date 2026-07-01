package org.dalesbred.internal.instantiation;

import org.dalesbred.DatabaseSQLException;
import org.dalesbred.internal.jdbc.ResultSetUtils;
import org.dalesbred.internal.jdbc.SqlUtils;
import org.dalesbred.internal.utils.TypeUtils;
import org.dalesbred.result.UnexpectedResultException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

final class SqlArrayConversion {

    private final @NotNull Type elementType;

    private final @NotNull InstantiatorProvider instantiatorRegistry;

    private SqlArrayConversion(@NotNull Type elementType, @NotNull InstantiatorProvider instantiatorRegistry) {
        this.elementType = elementType;
        this.instantiatorRegistry = instantiatorRegistry;
    }

    public static @NotNull TypeConversion sqlArray(@NotNull Type elementType, @NotNull InstantiatorProvider instantiatorProvider,
                                                   @NotNull Function<List<?>, ?> createResult) {
        SqlArrayConversion conversion = new SqlArrayConversion(elementType, instantiatorProvider);

        return TypeConversion.fromNonNullFunction((Array array) -> createResult.apply(conversion.readArray(array)));
    }

    private @NotNull List<?> readArray(@NotNull Array array) {
        try {
            boolean allowNulls = !TypeUtils.isPrimitive(elementType);
            ResultSet resultSet = array.getResultSet();
            try {
                NamedTypeList types = NamedTypeList.builder(1).add(
                    "value", ResultSetUtils.getColumnType(resultSet.getMetaData(), instantiatorRegistry.getDialect(), 2)
                ).build();
                Instantiator<?> ctor = instantiatorRegistry.findInstantiator(elementType, types);
                ArrayList<Object> result = new ArrayList<>();

                // For performance reasons we reuse the same arguments-array and InstantiatorArguments-object for all rows.
                // This should be fine as long as the instantiators don't hang on to their arguments for too long.
                Object[] arguments = new Object[1];
                InstantiatorArguments instantiatorArguments = new InstantiatorArguments(types, arguments);

                while (resultSet.next()) {
                    arguments[0] = resultSet.getObject(2);

                    Object value = ctor.instantiate(instantiatorArguments);
                    if (value != null || allowNulls)
                        result.add(value);
                    else
                        throw new UnexpectedResultException("Expected " + elementType + ", but got null");
                }

                return result;

            } finally {
                try {
                    resultSet.close();
                } finally {
                    SqlUtils.freeArray(array);
                }
            }

        } catch (SQLException e) {
            throw new DatabaseSQLException(e);
        }
    }
}
