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

package fi.evident.dalesbred.utils;

import fi.evident.dalesbred.DatabaseException;
import fi.evident.dalesbred.instantiation.NamedTypeList;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public final class ResultSetUtils {

    private ResultSetUtils() { }

    @NotNull
    public static NamedTypeList getTypes(@NotNull ResultSetMetaData metaData) throws SQLException {
        int columns = metaData.getColumnCount();

        NamedTypeList.Builder result = NamedTypeList.builder(columns);

        for (int i = 0; i < columns; i++)
            result.add(metaData.getColumnName(i+1), ResultSetUtils.getColumnType(metaData, i+1));

        return result.build();
    }

    @NotNull
    public static Class<?> getColumnType(@NotNull ResultSetMetaData metaData, int column) throws SQLException {
        String className = metaData.getColumnClassName(column);
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("Could not find class '" + className + "' specified by ResultSet.", e);
        }
    }
}
