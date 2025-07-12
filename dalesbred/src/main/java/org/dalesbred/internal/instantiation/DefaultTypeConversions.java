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

import org.dalesbred.DatabaseException;
import org.dalesbred.DatabaseSQLException;
import org.dalesbred.conversion.TypeConversionRegistry;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import javax.xml.transform.dom.DOMSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.*;
import java.time.*;
import java.util.TimeZone;

final class DefaultTypeConversions {

    private static final int BUFFER_SIZE = 1024;

    private static final int EPOCH_YEAR = 1900;

    private DefaultTypeConversions() { }

    public static void register(@NotNull TypeConversionRegistry registry) {
        registry.registerConversions(String.class, URL.class, DefaultTypeConversions::convertStringToUrl, URL::toString);
        registry.registerConversions(String.class, URI.class, DefaultTypeConversions::convertStringToUri, URI::toString);
        registry.registerConversions(String.class, TimeZone.class, TimeZone::getTimeZone, TimeZone::getID);

        registry.registerConversionFromDatabase(Number.class, Short.class, Number::shortValue);
        registry.registerConversionFromDatabase(Number.class, Integer.class, Number::intValue);
        registry.registerConversionFromDatabase(Number.class, Long.class, Number::longValue);
        registry.registerConversionFromDatabase(Number.class, Float.class, Number::floatValue);
        registry.registerConversionFromDatabase(Number.class, Double.class, Number::doubleValue);
        registry.registerConversionFromDatabase(Number.class, BigInteger.class, DefaultTypeConversions::convertNumberToBigInteger);
        registry.registerConversionFromDatabase(Number.class, BigDecimal.class, DefaultTypeConversions::convertNumberToBigDecimal);
        registry.registerConversionFromDatabase(Clob.class, String.class, DefaultTypeConversions::convertClobToString);
        registry.registerConversionFromDatabase(Clob.class, Reader.class, DefaultTypeConversions::convertClobToReader);
        registry.registerConversionFromDatabase(Blob.class, byte[].class, DefaultTypeConversions::convertBlobToByteArray);
        registry.registerConversionFromDatabase(Blob.class, InputStream.class, DefaultTypeConversions::convertBlobToInputStream);
        registry.registerConversionFromDatabase(SQLXML.class, Document.class, DefaultTypeConversions::convertSQLXMLToDocument);

        registry.registerConversionToDatabase(BigInteger.class, BigDecimal::new);

        // java.time
        registry.registerConversions(Timestamp.class, Instant.class, Timestamp::toInstant, Timestamp::from);
        registry.registerConversions(Timestamp.class, LocalDateTime.class, Timestamp::toLocalDateTime, Timestamp::valueOf);
        registry.registerConversions(Time.class, LocalTime.class, Time::toLocalTime, Time::valueOf);
        registry.registerConversions(String.class, ZoneId.class, ZoneId::of, ZoneId::getId);
        registry.registerConversionFromDatabase(java.util.Date.class, LocalDate.class, DefaultTypeConversions::convertDateToLocalDate);
        registry.registerConversionToDatabase(LocalDate.class, Date::valueOf);
    }

    @SuppressWarnings("ObjectToString")
    private static @NotNull BigInteger convertNumberToBigInteger(@NotNull Number value) {
        return (value instanceof BigInteger) ? (BigInteger) value
             : (value instanceof BigDecimal) ? ((BigDecimal) value).toBigInteger()
             : (value instanceof Integer)    ? BigInteger.valueOf(value.longValue())
             : (value instanceof Long)       ? BigInteger.valueOf(value.longValue())
             : new BigInteger(value.toString());
    }

    @SuppressWarnings("ObjectToString")
    private static BigDecimal convertNumberToBigDecimal(@NotNull Number value) {
        return (value instanceof BigDecimal) ? (BigDecimal) value
                : (value instanceof BigInteger) ? new BigDecimal((BigInteger) value)
                : (value instanceof Integer)    ? BigDecimal.valueOf(value.longValue())
                : (value instanceof Long)       ? BigDecimal.valueOf(value.longValue())
                : new BigDecimal(value.toString());
    }

    private static @NotNull URL convertStringToUrl(@NotNull String value) {
        try {
            return new URI(value).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static @NotNull URI convertStringToUri(@NotNull String value) {
        try {
            return new URI(value);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static @NotNull String convertClobToString(@NotNull Clob value) {
        try (Reader reader = value.getCharacterStream()) {
            StringBuilder sb = new StringBuilder((int) value.length());

            char[] buf = new char[BUFFER_SIZE];
            int n;

            while ((n = reader.read(buf)) != -1)
                sb.append(buf, 0, n);

            return sb.toString();

        } catch (SQLException e) {
            throw new DatabaseSQLException(e);
        } catch (IOException e) {
            throw new DatabaseException("failed to convert Clob to String", e);
        }
    }

    private static byte @NotNull [] convertBlobToByteArray(@NotNull Blob value) {
        try (InputStream in = value.getBinaryStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream((int) value.length());

            byte[] buf = new byte[BUFFER_SIZE];
            int n;

            while ((n = in.read(buf)) != -1)
                out.write(buf, 0, n);

            return out.toByteArray();

        } catch (SQLException e) {
            throw new DatabaseSQLException(e);
        } catch (IOException e) {
            throw new DatabaseException("failed to convert Blob to byte-array", e);
        }
    }

    private static @NotNull InputStream convertBlobToInputStream(@NotNull Blob value) {
        try {
            return value.getBinaryStream();
        } catch (SQLException e) {
            throw new DatabaseSQLException(e);
        }
    }

    private static @NotNull Reader convertClobToReader(@NotNull Clob value) {
        try {
            return value.getCharacterStream();
        } catch (SQLException e) {
            throw new DatabaseSQLException(e);
        }
    }

    private static @NotNull Document convertSQLXMLToDocument(@NotNull SQLXML value) {
        try {
            return (Document) value.getSource(DOMSource.class).getNode();
        } catch (SQLException e) {
            throw new DatabaseSQLException(e);
        }
    }

    @SuppressWarnings("deprecation")
    private static @NotNull LocalDate convertDateToLocalDate(@NotNull java.util.Date value) {
        return LocalDate.of(value.getYear() + EPOCH_YEAR, value.getMonth() + 1, value.getDate());
    }
}
