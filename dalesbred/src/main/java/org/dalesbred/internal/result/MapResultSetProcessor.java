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

        NamedTypeList types = ResultSetUtils.getTypes(resultSet.getMetaData());
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
