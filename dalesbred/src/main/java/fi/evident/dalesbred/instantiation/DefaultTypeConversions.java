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

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

final class DefaultTypeConversions {

    private DefaultTypeConversions() { }

    public static void register(@NotNull TypeConversionRegistry typeConversionRegistry) {
        typeConversionRegistry.registerConversionFromDatabaseType(new StringToUrlTypeConversion());
        typeConversionRegistry.registerConversionFromDatabaseType(new StringToUriTypeConversion());
        typeConversionRegistry.registerConversionFromDatabaseType(new NumberToShortTypeConversion());
        typeConversionRegistry.registerConversionFromDatabaseType(new NumberToIntTypeConversion());
        typeConversionRegistry.registerConversionFromDatabaseType(new NumberToLongTypeConversion());
        typeConversionRegistry.registerConversionFromDatabaseType(new NumberToFloatTypeConversion());
        typeConversionRegistry.registerConversionFromDatabaseType(new NumberToDoubleTypeConversion());
        typeConversionRegistry.registerConversionFromDatabaseType(new NumberToBigIntegerTypeConversion());
        typeConversionRegistry.registerConversionFromDatabaseType(new NumberToBigDecimalTypeConversion());

        typeConversionRegistry.registerConversionToDatabaseType(new BigIntegerToBigDecimalTypeConversion());
        typeConversionRegistry.registerConversionToDatabaseType(new ToStringTypeConversion<URL>(URL.class));
        typeConversionRegistry.registerConversionToDatabaseType(new ToStringTypeConversion<URI>(URI.class));
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
        public BigInteger convert(@NotNull Number value) {
            return (value instanceof BigInteger) ? (BigInteger) value
                 : (value instanceof BigDecimal) ? ((BigDecimal) value).toBigInteger()
                 : (value instanceof Integer)    ? BigInteger.valueOf(value.intValue())
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
        public BigDecimal convert(@NotNull Number value) {
            return (value instanceof BigDecimal) ? (BigDecimal) value
                    : (value instanceof BigInteger) ? new BigDecimal((BigInteger) value)
                    : (value instanceof Integer)    ? BigDecimal.valueOf(value.intValue())
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

        ToStringTypeConversion(Class<S> source) {
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
}
