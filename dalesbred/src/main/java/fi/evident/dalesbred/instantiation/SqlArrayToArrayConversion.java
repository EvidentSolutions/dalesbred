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

package fi.evident.dalesbred.instantiation;

import fi.evident.dalesbred.DatabaseSQLException;
import fi.evident.dalesbred.UnexpectedResultException;
import fi.evident.dalesbred.utils.ResultSetUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fi.evident.dalesbred.utils.SqlUtils.freeArray;

/**
 * Converts database arrays to array types.
 */
final class SqlArrayToArrayConversion<T> extends TypeConversion<java.sql.Array, T> {

    @NotNull
    private final Class<?> elementType;

    @NotNull
    private final DefaultInstantiatorRegistry instantiatorRegistry;

    public SqlArrayToArrayConversion(@NotNull Class<T> arrayType, @NotNull DefaultInstantiatorRegistry instantiatorRegistry) {
        super(java.sql.Array.class, arrayType);
        this.instantiatorRegistry = instantiatorRegistry;

        if (!arrayType.isArray())
            throw new IllegalArgumentException("not an array type: " + arrayType);

        this.elementType = arrayType.getComponentType();
    }

    @NotNull
    @Override
    public T convert(@NotNull java.sql.Array value) {
        List<?> list = readArray(value);

        int length = list.size();

        @SuppressWarnings("unchecked")
        T result = (T) Array.newInstance(elementType, length);
        for (int i = 0; i < length; i++)
            Array.set(result, i, list.get(i));

        return result;
    }

    @NotNull
    private List<?> readArray(@NotNull java.sql.Array array) {
        try {
            boolean allowNulls = !elementType.isPrimitive();
            ResultSet resultSet = array.getResultSet();
            try {
                NamedTypeList types = NamedTypeList.builder(1).add("value", ResultSetUtils.getColumnType(resultSet.getMetaData(), 2)).build();
                Instantiator<?> ctor = instantiatorRegistry.findInstantiator(elementType, types);
                ArrayList<Object> result = new ArrayList<Object>();

                // For performance reasons we reuse the same arguments-array and InstantiatorArguments-object for all rows.
                // This should be fine as long as the instantiators don't hang on to their arguments for too long.
                Object[] arguments = new Object[1];
                InstantiatorArguments instantiatorArguments = new InstantiatorArguments(types, Arrays.asList(arguments));

                while (resultSet.next()) {
                    arguments[0] = resultSet.getObject(2);

                    Object value = ctor.instantiate(instantiatorArguments);
                    if (value != null || allowNulls)
                        result.add(value);
                    else
                        throw new UnexpectedResultException("Expected " + elementType.getName() + ", but got null");
                }

                return result;

            } finally {
                try {
                    resultSet.close();
                } finally {
                    freeArray(array);
                }
            }

        } catch (SQLException e) {
            throw new DatabaseSQLException(e);
        }
    }
}
