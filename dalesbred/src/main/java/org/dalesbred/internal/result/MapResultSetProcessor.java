package org.dalesbred.internal.result;

import org.dalesbred.internal.instantiation.*;
import org.dalesbred.internal.jdbc.ResultSetUtils;
import org.dalesbred.internal.utils.Primitives;
import org.dalesbred.result.ResultSetProcessor;
import org.dalesbred.result.UnexpectedResultException;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * ResultSetProcessor that expects results with two columns and creates map from them.
 *
 * <p>This processor updates the map in order in which the rows are read. Therefore, if
 * the keys of the result are not distinct, the result contains the last binding of given key.
 */
public final class MapResultSetProcessor<K,V> implements ResultSetProcessor<Map<K,V>> {

    private final @NotNull Class<K> keyType;

    private final @NotNull Class<V> valueType;

    private final @NotNull InstantiatorProvider instantiatorRegistry;

    public MapResultSetProcessor(@NotNull Class<K> keyType,
                                 @NotNull Class<V> valueType,
                                 @NotNull InstantiatorProvider instantiatorRegistry) {
        this.keyType = Primitives.wrap(requireNonNull(keyType));
        this.valueType = Primitives.wrap(requireNonNull(valueType));
        this.instantiatorRegistry = requireNonNull(instantiatorRegistry);
    }

    @Override
    public @NotNull Map<K, V> process(@NotNull ResultSet resultSet) throws SQLException {

        NamedTypeList types = ResultSetUtils.getTypes(resultSet.getMetaData(), instantiatorRegistry.getDialect());
        if (types.size() < 2)
            throw new UnexpectedResultException("Expected ResultSet with at least 2 columns, but got " + types.size() + " columns.");

        NamedTypeList valueTypes = types.subList(1, types.size());
        TypeConversion keyConversion = instantiatorRegistry.getConversionFromDbValue(types.getType(0), keyType);
        Instantiator<V> valueInstantiator = instantiatorRegistry.findInstantiator(valueType, valueTypes);

        // For performance reasons we reuse the same arguments-array and InstantiatorArguments-object for all rows.
        // This should be fine as long as the instantiators don't hang on to their arguments for too long.
        Object[] valueArguments = new Object[valueTypes.size()];
        InstantiatorArguments instantiatorArguments = new InstantiatorArguments(valueTypes, valueArguments);

        Map<K, V> result = new LinkedHashMap<>();
        while (resultSet.next()) {
            K key = keyType.cast(keyConversion.convert(resultSet.getObject(1)));

            for (int i = 0; i < valueArguments.length; i++)
                valueArguments[i] = resultSet.getObject(i+2);

            V value = valueInstantiator.instantiate(instantiatorArguments);

            result.put(key, value);
        }

        return result;
    }

}
