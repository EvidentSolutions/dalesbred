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
        return new TypeConversionPair<Object, T>() {
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
