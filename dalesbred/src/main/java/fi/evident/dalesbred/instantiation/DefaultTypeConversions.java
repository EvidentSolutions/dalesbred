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

package fi.evident.dalesbred.instantiation;

import fi.evident.dalesbred.DatabaseException;
import fi.evident.dalesbred.DatabaseSQLException;
import org.jetbrains.annotations.NotNull;

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
        registry.registerConversionFromDatabaseType(new BlobToByteArrayTypeConversion());

        registry.registerConversionToDatabaseType(new BigIntegerToBigDecimalTypeConversion());
        registry.registerConversionToDatabaseType(new ToStringTypeConversion<URL>(URL.class));
        registry.registerConversionToDatabaseType(new ToStringTypeConversion<URI>(URI.class));
        registry.registerConversionToDatabaseType(new TimeZoneToStringTypeConversion());
    }

    private static class NumberToShortTypeConversion extends TypeConversion<Number, Short> {

        NumberToShortTypeConversion() {
            super(Number.class, Short.class);
        }

        @NotNull
        @Override
        public Short convert(@NotNull Number value) {
            return value.shortValue();
        }
    }

    private static class NumberToIntTypeConversion extends TypeConversion<Number, Integer> {

        NumberToIntTypeConversion() {
            super(Number.class, Integer.class);
        }

        @NotNull
        @Override
        public Integer convert(@NotNull Number value) {
            return value.intValue();
        }
    }

    private static class NumberToLongTypeConversion extends TypeConversion<Number, Long> {

        NumberToLongTypeConversion() {
            super(Number.class, Long.class);
        }

        @NotNull
        @Override
        public Long convert(@NotNull Number value) {
            return value.longValue();
        }
    }

    private static class NumberToFloatTypeConversion extends TypeConversion<Number, Float> {

        NumberToFloatTypeConversion() {
            super(Number.class, Float.class);
        }

        @NotNull
        @Override
        public Float convert(@NotNull Number value) {
            return value.floatValue();
        }
    }

    private static class NumberToDoubleTypeConversion extends TypeConversion<Number, Double> {

        NumberToDoubleTypeConversion() {
            super(Number.class, Double.class);
        }

        @NotNull
        @Override
        public Double convert(@NotNull Number value) {
            return value.doubleValue();
        }
    }

    private static class NumberToBigIntegerTypeConversion extends TypeConversion<Number, BigInteger> {

        NumberToBigIntegerTypeConversion() {
            super(Number.class, BigInteger.class);
        }

        @NotNull
        @Override
        @SuppressWarnings("ObjectToString")
        public BigInteger convert(@NotNull Number value) {
            return (value instanceof BigInteger) ? (BigInteger) value
                 : (value instanceof BigDecimal) ? ((BigDecimal) value).toBigInteger()
                 : (value instanceof Integer)    ? BigInteger.valueOf(value.longValue())
                 : (value instanceof Long)       ? BigInteger.valueOf(value.longValue())
                 : new BigInteger(value.toString());
        }
    }

    private static class NumberToBigDecimalTypeConversion extends TypeConversion<Number, BigDecimal> {

        NumberToBigDecimalTypeConversion() {
            super(Number.class, BigDecimal.class);
        }

        @NotNull
        @Override
        @SuppressWarnings("ObjectToString")
        public BigDecimal convert(@NotNull Number value) {
            return (value instanceof BigDecimal) ? (BigDecimal) value
                    : (value instanceof BigInteger) ? new BigDecimal((BigInteger) value)
                    : (value instanceof Integer)    ? BigDecimal.valueOf(value.longValue())
                    : (value instanceof Long)       ? BigDecimal.valueOf(value.longValue())
                    : new BigDecimal(value.toString());
        }
    }

    private static class BigIntegerToBigDecimalTypeConversion extends TypeConversion<BigInteger, BigDecimal> {

        BigIntegerToBigDecimalTypeConversion() {
            super(BigInteger.class, BigDecimal.class);
        }

        @NotNull
        @Override
        public BigDecimal convert(@NotNull BigInteger value) {
            return new BigDecimal(value);
        }
    }

    private static class StringToUrlTypeConversion extends TypeConversion<String,URL> {

        StringToUrlTypeConversion() {
            super(String.class, URL.class);
        }

        @NotNull
        @Override
        public URL convert(@NotNull String value) {
            try {
                return new URL(value);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    private static class ToStringTypeConversion<S> extends TypeConversion<S,String> {

        ToStringTypeConversion(@NotNull Class<S> source) {
            super(source, String.class);
        }

        @NotNull
        @Override
        public String convert(@NotNull S value) {
            return value.toString();
        }
    }

    private static class StringToUriTypeConversion extends TypeConversion<String,URI> {

        StringToUriTypeConversion() {
            super(String.class, URI.class);
        }

        @NotNull
        @Override
        public URI convert(@NotNull String value) {
            try {
                return new URI(value);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    private static class StringToTimeZoneTypeConversion extends TypeConversion<String,TimeZone> {

        StringToTimeZoneTypeConversion() {
            super(String.class, TimeZone.class);
        }

        @NotNull
        @Override
        public TimeZone convert(@NotNull String value) {
            return TimeZone.getTimeZone(value);
        }
    }

    private static class TimeZoneToStringTypeConversion extends TypeConversion<TimeZone,String> {

        TimeZoneToStringTypeConversion() {
            super(TimeZone.class, String.class);
        }

        @NotNull
        @Override
        public String convert(@NotNull TimeZone value) {
            return value.getID();
        }
    }

    private static class ClobToStringTypeConversion extends TypeConversion<Clob,String> {

        ClobToStringTypeConversion() {
            super(Clob.class, String.class);
        }

        @NotNull
        @Override
        public String convert(@NotNull Clob value) {
            try {
                Reader reader = value.getCharacterStream();
                try {
                    StringBuilder sb = new StringBuilder((int) value.length());

                    char[] buf = new char[BUFFER_SIZE];
                    int n;

                    while ((n = reader.read(buf)) != -1)
                        sb.append(buf, 0, n);

                    return sb.toString();

                } finally {
                    reader.close();
                }
            } catch (SQLException e) {
                throw new DatabaseSQLException(e);
            } catch (IOException e) {
                throw new DatabaseException("failed to convert Clob to String", e);
            }
        }
    }

    private static class BlobToByteArrayTypeConversion extends TypeConversion<Blob,byte[]> {

        BlobToByteArrayTypeConversion() {
            super(Blob.class, byte[].class);
        }

        @NotNull
        @Override
        public byte[] convert(@NotNull Blob value) {
            try {
                InputStream in = value.getBinaryStream();
                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream((int) value.length());

                    byte[] buf = new byte[BUFFER_SIZE];
                    int n;

                    while ((n = in.read(buf)) != -1)
                        out.write(buf, 0, n);

                    return out.toByteArray();

                } finally {
                    in.close();
                }
            } catch (SQLException e) {
                throw new DatabaseSQLException(e);
            } catch (IOException e) {
                throw new DatabaseException("failed to convert Blob to byte-array", e);
            }
        }
    }
}
