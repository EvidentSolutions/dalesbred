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

package org.dalesbred.internal.result;

import org.dalesbred.internal.instantiation.*;
import org.dalesbred.internal.jdbc.ResultSetUtils;
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
 */
public final class MapResultSetProcessor<K,V> implements ResultSetProcessor<Map<K,V>> {

    @NotNull
    private final Class<K> keyType;

    @NotNull
    private final Class<V> valueType;

    @NotNull
    private final InstantiatorProvider instantiatorRegistry;

    public MapResultSetProcessor(@NotNull Class<K> keyType,
                                 @NotNull Class<V> valueType,
                                 @NotNull InstantiatorProvider instantiatorRegistry) {
        this.keyType = requireNonNull(keyType);
        this.valueType = requireNonNull(valueType);
        this.instantiatorRegistry = requireNonNull(instantiatorRegistry);
    }

    @NotNull
    @Override
    public Map<K, V> process(@NotNull ResultSet resultSet) throws SQLException {
        Map<K,V> result = new LinkedHashMap<>();

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
