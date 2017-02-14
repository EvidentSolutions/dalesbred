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

import org.dalesbred.DatabaseException;
import org.dalesbred.datatype.SqlArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Support for Oracle.
 */
public class OracleDialect extends Dialect {

    @Override
    public void bindArgument(@NotNull PreparedStatement ps, int index, @Nullable  Object value) throws SQLException {
        if (value instanceof SqlArray) {
            ps.setArray(index, createOracleArray(ps, (SqlArray) value));

        } else {
            super.bindArgument(ps, index, value);
        }
    }

    private static @NotNull Array createOracleArray(@NotNull  PreparedStatement ps, @NotNull  SqlArray arr) throws SQLException {
        // This method is ugly. We'd like to say just:
        //
        //     OracleConnection oracleConnection = ps.getConnection().unwrap(OracleConnection.class);
        //     return oracleConnection.createARRAY(arr.getType(), arr.getValues().toArray());
        //
        // Unfortunately depending on Oracle JDBC driver is quite problematic because it's not
        // available in any repository. So, to keep building Dalesbred simpler (especially for
        // those who don't need Oracle), we do the same thing using reflection.
        try {
            Class<?> oracleConnectionClass = Class.forName("oracle.jdbc.OracleConnection");
            Method createArrayMethod = oracleConnectionClass.getMethod("createARRAY", String.class, Object.class);

            Object oracleConnection = ps.getConnection().unwrap(oracleConnectionClass);
            return (Array) createArrayMethod.invoke(oracleConnection, arr.getType(), arr.getValues().toArray());

        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new DatabaseException(e);
        }
    }
}
