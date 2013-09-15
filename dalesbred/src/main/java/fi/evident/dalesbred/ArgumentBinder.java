/*
 * Copyright (c) 2013 Evident Solutions Oy
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

package fi.evident.dalesbred;

import fi.evident.dalesbred.lob.InputStreamWithSize;
import fi.evident.dalesbred.lob.ReaderWithSize;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;

import javax.xml.transform.dom.DOMResult;
import java.io.InputStream;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLXML;

final class ArgumentBinder {

    private ArgumentBinder() {
    }

    static void bindArgument(@NotNull PreparedStatement ps, int index, @Nullable Object value) throws SQLException {
        if (value instanceof InputStream) {
            bindInputStream(ps, index, (InputStream) value);

        } else if (value instanceof Reader) {
            bindReader(ps, index, (Reader) value);

        } else if (value instanceof Document) {
            bindXmlDocument(ps, index, (Document) value);
        } else {
            ps.setObject(index, value);
        }
    }

    private static void bindInputStream(@NotNull PreparedStatement ps, int index, @NotNull InputStream stream) throws SQLException {
        // We check whether the InputStream is actually InputStreamWithSize, for two reasons:
        //   1) the database/driver can optimize the call better if it knows the size in advance
        //   2) calls without size were introduced in JDBC4 and not all drivers support them
        if (stream instanceof InputStreamWithSize) {
            InputStreamWithSize streamWithSize = (InputStreamWithSize) stream;
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
        if (reader instanceof ReaderWithSize) {
            ReaderWithSize readerWithSize = (ReaderWithSize) reader;
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
}
