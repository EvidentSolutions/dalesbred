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

package org.dalesbred.result;

import org.dalesbred.UnexpectedResultException;
import org.dalesbred.instantiation.DefaultInstantiatorRegistry;
import org.dalesbred.instantiation.NamedTypeList;
import org.dalesbred.instantiation.TypeConversion;
import org.dalesbred.internal.jdbc.ResultSetUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private final DefaultInstantiatorRegistry instantiatorRegistry;

    public MapResultSetProcessor(@NotNull Class<K> keyType,
                                 @NotNull Class<V> valueType,
                                 @NotNull DefaultInstantiatorRegistry instantiatorRegistry) {
        this.keyType = requireNonNull(keyType);
        this.valueType = requireNonNull(valueType);
        this.instantiatorRegistry = requireNonNull(instantiatorRegistry);
    }

    @NotNull
    @Override
    public Map<K, V> process(@NotNull ResultSet resultSet) throws SQLException {
        Map<K,V> result = new LinkedHashMap<>();

        NamedTypeList types = ResultSetUtils.getTypes(resultSet.getMetaData());
        if (types.size() != 2)
            throw new UnexpectedResultException("Expected ResultSet with 2 columns, but got " + types.size() + " columns.");

        TypeConversion<Object, K> keyConversion = getConversion(types.getType(0), keyType);
        TypeConversion<Object, V> valueConversion = getConversion(types.getType(1), valueType);

        while (resultSet.next()) {
            K key = convert(keyConversion, resultSet.getObject(1));
            V value = convert(valueConversion, resultSet.getObject(2));

            result.put(key, value);
        }

        return result;
    }

    @Nullable
    private static <T> T convert(TypeConversion<Object, T> conversion, @Nullable Object value) {
        if (value != null)
            return conversion.convert(value);
        else
            return null;
    }

    @NotNull
    private <T> TypeConversion<Object, T> getConversion(@NotNull Class<?> sourceType, @NotNull Class<T> targetType) {
        return instantiatorRegistry.getCoercionFromDbValue(sourceType, targetType).unsafeCast(targetType);
    }
}
