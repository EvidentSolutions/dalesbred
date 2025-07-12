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

package org.dalesbred.internal.jdbc;

import org.dalesbred.datatype.InputStreamWithSize;
import org.dalesbred.datatype.ReaderWithSize;
import org.dalesbred.datatype.SqlArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;

import javax.xml.transform.dom.DOMResult;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLXML;

public final class ArgumentBinder {

    private ArgumentBinder() {
    }

    public static void bindArgument(@NotNull PreparedStatement ps, int index, @Nullable Object value) throws SQLException {
        switch (value) {
            case InputStream inputStream -> bindInputStream(ps, index, inputStream);
            case Reader reader -> bindReader(ps, index, reader);
            case Document document -> bindXmlDocument(ps, index, document);
            case SqlArray sqlArray -> bindArray(ps, index, sqlArray);
            case null, default -> ps.setObject(index, value);
        }
    }

    private static void bindInputStream(@NotNull PreparedStatement ps, int index, @NotNull InputStream stream) throws SQLException {
        // We check whether the InputStream is actually InputStreamWithSize, for two reasons:
        //   1) the database/driver can optimize the call better if it knows the size in advance
        //   2) calls without size were introduced in JDBC4 and not all drivers support them
        if (stream instanceof InputStreamWithSize streamWithSize) {
            long size = streamWithSize.getSize();

            // The overload which takes 'long' as parameter was introduced in JDBC4 and is not
            // universally supported so we'll call the 'int' overload if possible.
            if (size <= Integer.MAX_VALUE)
                ps.setBinaryStream(index, streamWithSize, (int) size);
            else
                ps.setBinaryStream(index, streamWithSize, size);

        } else {
            ps.setBinaryStream(index, stream);
        }
    }

    private static void bindReader(@NotNull PreparedStatement ps, int index, @NotNull Reader reader) throws SQLException {
        // The structure followed bindInputStream, see the comments there.
        if (reader instanceof ReaderWithSize readerWithSize) {
            long size = readerWithSize.getSize();
            if (size <= Integer.MAX_VALUE)
                ps.setCharacterStream(index, readerWithSize, (int) size);
            else
                ps.setCharacterStream(index, readerWithSize, size);
        } else {
            ps.setCharacterStream(index, reader);
        }
    }

    private static void bindXmlDocument(@NotNull PreparedStatement ps, int index, @NotNull Document doc) throws SQLException {
        SQLXML sqlxml = ps.getConnection().createSQLXML();
        // TODO: arrange for the object to be freed after the PreparedStatement has been executed

        sqlxml.setResult(DOMResult.class).setNode(doc);

        ps.setSQLXML(index, sqlxml);
    }

    private static void bindArray(PreparedStatement ps, int index, SqlArray value) throws SQLException {
        // TODO: arrange for the array to be freed after the PreparedStatement has been executed
        Array array = ps.getConnection().createArrayOf(value.getType(), value.getValues().toArray());
        ps.setArray(index, array);
    }
}
