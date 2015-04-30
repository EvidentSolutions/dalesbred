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

package org.dalesbred.instantiation;

import org.dalesbred.DatabaseSQLException;
import org.dalesbred.UnexpectedResultException;
import org.dalesbred.internal.jdbc.ResultSetUtils;
import org.dalesbred.internal.jdbc.SqlUtils;
import org.dalesbred.internal.utils.TypeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

abstract class AbstractSqlArrayConversion<T> extends TypeConversion<Array, T> {

    @NotNull
    private final Type elementType;

    @NotNull
    private final DefaultInstantiatorRegistry instantiatorRegistry;

    public AbstractSqlArrayConversion(@NotNull Type target, @NotNull Type elementType, @NotNull DefaultInstantiatorRegistry instantiatorRegistry) {
        super(Array.class, target);

        this.elementType = elementType;
        this.instantiatorRegistry = instantiatorRegistry;
    }

    @Nullable
    @Override
    public T convert(@Nullable Array value) {
        if (value != null) {
            return createResult(readArray(value));
        } else {
            return null;
        }
    }

    @NotNull
    protected abstract T createResult(@NotNull List<?> list);

    @NotNull
    private List<?> readArray(@NotNull Array array) {
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
