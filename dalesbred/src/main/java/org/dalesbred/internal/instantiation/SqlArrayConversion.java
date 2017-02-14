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

    private SqlArrayConversion(@NotNull Type elementType,
                               @NotNull InstantiatorProvider instantiatorRegistry) {

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
                NamedTypeList types = NamedTypeList.builder(1).add("value", ResultSetUtils.getColumnType(resultSet.getMetaData(), 2)).build();
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
