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

import org.dalesbred.DatabaseException;
import org.dalesbred.DatabaseSQLException;
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
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.TimeZone;

final class DefaultTypeConversions {

    private static final int BUFFER_SIZE = 1024;

    private DefaultTypeConversions() { }

    public static void register(@NotNull TypeConversionRegistry registry) {
        registry.registerConversionFromDatabaseType(new StringToUrlTypeConversion());
        registry.registerConversionFromDatabaseType(new StringToUriTypeConversion());
        registry.registerConversionFromDatabaseType(new StringToTimeZoneTypeConversion());
        registry.registerConversionFromDatabaseType(new NumberToShortTypeConversion());
        registry.registerConversionFromDatabaseType(new NumberToIntTypeConversion());
        registry.registerConversionFromDatabaseType(new NumberToLongTypeConversion());
        registry.registerConversionFromDatabaseType(new NumberToFloatTypeConversion());
        registry.registerConversionFromDatabaseType(new NumberToDoubleTypeConversion());
        registry.registerConversionFromDatabaseType(new NumberToBigIntegerTypeConversion());
        registry.registerConversionFromDatabaseType(new NumberToBigDecimalTypeConversion());
        registry.registerConversionFromDatabaseType(new ClobToStringTypeConversion());
        registry.registerConversionFromDatabaseType(new ClobToReaderTypeConversion());
        registry.registerConversionFromDatabaseType(new BlobToByteArrayTypeConversion());
        registry.registerConversionFromDatabaseType(new BlobToInputStreamTypeConversion());
        registry.registerConversionFromDatabaseType(new SQLXMLToDocumentConversion());

        registry.registerConversionToDatabaseType(new BigIntegerToBigDecimalTypeConversion());
        registry.registerConversionToDatabaseType(new ToStringTypeConversion<>(URL.class));
        registry.registerConversionToDatabaseType(new ToStringTypeConversion<>(URI.class));
        registry.registerConversionToDatabaseType(new TimeZoneToStringTypeConversion());
    }

    private static class NumberToShortTypeConversion extends SimpleNonNullTypeConversion<Number, Short> {

        NumberToShortTypeConversion() {
            super(Number.class, Short.class);
        }

        @NotNull
        @Override
        public Short convertNonNull(@NotNull Number value) {
            return value.shortValue();
        }
    }

    private static class NumberToIntTypeConversion extends SimpleNonNullTypeConversion<Number, Integer> {

        NumberToIntTypeConversion() {
            super(Number.class, Integer.class);
        }

        @NotNull
        @Override
        public Integer convertNonNull(@NotNull Number value) {
            return value.intValue();
        }
    }

    private static class NumberToLongTypeConversion extends SimpleNonNullTypeConversion<Number, Long> {

        NumberToLongTypeConversion() {
            super(Number.class, Long.class);
        }

        @NotNull
        @Override
        public Long convertNonNull(@NotNull Number value) {
            return value.longValue();
        }
    }

    private static class NumberToFloatTypeConversion extends SimpleNonNullTypeConversion<Number, Float> {

        NumberToFloatTypeConversion() {
            super(Number.class, Float.class);
        }

        @NotNull
        @Override
        public Float convertNonNull(@NotNull Number value) {
            return value.floatValue();
        }
    }

    private static class NumberToDoubleTypeConversion extends SimpleNonNullTypeConversion<Number, Double> {

        NumberToDoubleTypeConversion() {
            super(Number.class, Double.class);
        }

        @NotNull
        @Override
        public Double convertNonNull(@NotNull Number value) {
            return value.doubleValue();
        }
    }

    private static class NumberToBigIntegerTypeConversion extends SimpleNonNullTypeConversion<Number, BigInteger> {

        NumberToBigIntegerTypeConversion() {
            super(Number.class, BigInteger.class);
        }

        @NotNull
        @Override
        @SuppressWarnings("ObjectToString")
        public BigInteger convertNonNull(@NotNull Number value) {
            return (value instanceof BigInteger) ? (BigInteger) value
                 : (value instanceof BigDecimal) ? ((BigDecimal) value).toBigInteger()
                 : (value instanceof Integer)    ? BigInteger.valueOf(value.longValue())
                 : (value instanceof Long)       ? BigInteger.valueOf(value.longValue())
                 : new BigInteger(value.toString());
        }
    }

    private static class NumberToBigDecimalTypeConversion extends SimpleNonNullTypeConversion<Number, BigDecimal> {

        NumberToBigDecimalTypeConversion() {
            super(Number.class, BigDecimal.class);
        }

        @NotNull
        @Override
        @SuppressWarnings("ObjectToString")
        public BigDecimal convertNonNull(@NotNull Number value) {
            return (value instanceof BigDecimal) ? (BigDecimal) value
                    : (value instanceof BigInteger) ? new BigDecimal((BigInteger) value)
                    : (value instanceof Integer)    ? BigDecimal.valueOf(value.longValue())
                    : (value instanceof Long)       ? BigDecimal.valueOf(value.longValue())
                    : new BigDecimal(value.toString());
        }
    }

    private static class BigIntegerToBigDecimalTypeConversion extends SimpleNonNullTypeConversion<BigInteger, BigDecimal> {

        BigIntegerToBigDecimalTypeConversion() {
            super(BigInteger.class, BigDecimal.class);
        }

        @NotNull
        @Override
        public BigDecimal convertNonNull(@NotNull BigInteger value) {
            return new BigDecimal(value);
        }
    }

    private static class StringToUrlTypeConversion extends SimpleNonNullTypeConversion<String,URL> {

        StringToUrlTypeConversion() {
            super(String.class, URL.class);
        }

        @NotNull
        @Override
        public URL convertNonNull(@NotNull String value) {
            try {
                return new URL(value);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    private static class ToStringTypeConversion<S> extends SimpleNonNullTypeConversion<S,String> {

        ToStringTypeConversion(@NotNull Class<S> source) {
            super(source, String.class);
        }

        @NotNull
        @Override
        public String convertNonNull(@NotNull S value) {
            return value.toString();
        }
    }

    private static class StringToUriTypeConversion extends SimpleNonNullTypeConversion<String,URI> {

        StringToUriTypeConversion() {
            super(String.class, URI.class);
        }

        @NotNull
        @Override
        public URI convertNonNull(@NotNull String value) {
            try {
                return new URI(value);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    private static class StringToTimeZoneTypeConversion extends SimpleNonNullTypeConversion<String,TimeZone> {

        StringToTimeZoneTypeConversion() {
            super(String.class, TimeZone.class);
        }

        @NotNull
        @Override
        public TimeZone convertNonNull(@NotNull String value) {
            return TimeZone.getTimeZone(value);
        }
    }

    private static class TimeZoneToStringTypeConversion extends SimpleNonNullTypeConversion<TimeZone,String> {

        TimeZoneToStringTypeConversion() {
            super(TimeZone.class, String.class);
        }

        @NotNull
        @Override
        public String convertNonNull(@NotNull TimeZone value) {
            return value.getID();
        }
    }

    private static class ClobToStringTypeConversion extends SimpleNonNullTypeConversion<Clob,String> {

        ClobToStringTypeConversion() {
            super(Clob.class, String.class);
        }

        @NotNull
        @Override
        public String convertNonNull(@NotNull Clob value) {
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
    }

    private static class BlobToByteArrayTypeConversion extends SimpleNonNullTypeConversion<Blob,byte[]> {

        BlobToByteArrayTypeConversion() {
            super(Blob.class, byte[].class);
        }

        @NotNull
        @Override
        public byte[] convertNonNull(@NotNull Blob value) {
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
    }

    private static class BlobToInputStreamTypeConversion extends SimpleNonNullTypeConversion<Blob,InputStream> {

        BlobToInputStreamTypeConversion() {
            super(Blob.class, InputStream.class);
        }

        @NotNull
        @Override
        public InputStream convertNonNull(@NotNull Blob value) {
            try {
                return value.getBinaryStream();
            } catch (SQLException e) {
                throw new DatabaseSQLException(e);
            }
        }
    }

    private static class ClobToReaderTypeConversion extends SimpleNonNullTypeConversion<Clob,Reader> {

        ClobToReaderTypeConversion() {
            super(Clob.class, Reader.class);
        }

        @NotNull
        @Override
        public Reader convertNonNull(@NotNull Clob value) {
            try {
                return value.getCharacterStream();
            } catch (SQLException e) {
                throw new DatabaseSQLException(e);
            }
        }
    }

    private static class SQLXMLToDocumentConversion extends SimpleNonNullTypeConversion<SQLXML, Document> {

        public SQLXMLToDocumentConversion() {
            super(SQLXML.class, Document.class);
        }

        @NotNull
        @Override
        public Document convertNonNull(@NotNull SQLXML value) {
            try {
                return (Document) value.getSource(DOMSource.class).getNode();
            } catch (SQLException e) {
                throw new DatabaseSQLException(e);
            }
        }
    }
}
