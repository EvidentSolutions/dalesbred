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

package fi.evident.dalesbred.results;

import fi.evident.dalesbred.UnexpectedResultException;
import fi.evident.dalesbred.instantiation.InstantiatorRegistry;
import fi.evident.dalesbred.instantiation.NamedTypeList;
import fi.evident.dalesbred.instantiation.TypeConversion;
import fi.evident.dalesbred.utils.ResultSetUtils;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

/**
 * ResultSetProcessor that expects results with two columns and creates map from them.
 */
public final class MapResultSetProcessor<K,V> implements ResultSetProcessor<Map<K,V>> {

    @NotNull
    private final Class<K> keyType;

    @NotNull
    private final Class<V> valueType;

    @NotNull
    private final InstantiatorRegistry instantiatorRegistry;

    public MapResultSetProcessor(@NotNull Class<K> keyType,
                                 @NotNull Class<V> valueType,
                                 @NotNull InstantiatorRegistry instantiatorRegistry) {
        this.keyType = requireNonNull(keyType);
        this.valueType = requireNonNull(valueType);
        this.instantiatorRegistry = requireNonNull(instantiatorRegistry);
    }

    @NotNull
    @Override
    public Map<K, V> process(@NotNull ResultSet resultSet) throws SQLException {
        Map<K,V> result = new LinkedHashMap<K,V>();

        NamedTypeList types = ResultSetUtils.getTypes(resultSet.getMetaData());
        if (types.size() != 2)
            throw new UnexpectedResultException("Expected ResultSet with 2 columns, but got " + types.size() + " columns.");

        TypeConversion<Object, K> keyCoercion = getConversion(types.getType(0), keyType);
        TypeConversion<Object, V> valueCoercion = getConversion(types.getType(1), valueType);

        while (resultSet.next()) {
            K key = keyCoercion.convert(resultSet.getObject(1));
            V value = valueCoercion.convert(resultSet.getObject(2));
            result.put(key, value);
        }

        return result;
    }

    @NotNull
    private <T> TypeConversion<Object, T> getConversion(@NotNull Class<?> sourceType, @NotNull Class<T> targetType) {
        return instantiatorRegistry.getCoercionFromDbValue(sourceType, targetType).unsafeCast(targetType);
    }
}
