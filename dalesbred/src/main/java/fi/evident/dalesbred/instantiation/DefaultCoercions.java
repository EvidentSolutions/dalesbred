package fi.evident.dalesbred.instantiation;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

final class DefaultCoercions {

    public static void register(@NotNull Coercions coercions) {
        coercions.registerLoadConversion(new StringToUrlCoercion());
        coercions.registerLoadConversion(new StringToUriCoercion());
        coercions.registerLoadConversion(new NumberToShortCoercion());
        coercions.registerLoadConversion(new NumberToLongCoercion());
        coercions.registerLoadConversion(new NumberToFloatCoercion());
        coercions.registerLoadConversion(new NumberToDoubleCoercion());
        coercions.registerLoadConversion(new NumberToBigIntegerCoercion());
        coercions.registerLoadConversion(new NumberToBigDecimalCoercion());

        coercions.registerStoreConversion(new BigIntegerToBigDecimalCoercion());
        coercions.registerStoreConversion(new ToStringCoercion<URL>(URL.class));
        coercions.registerStoreConversion(new ToStringCoercion<URI>(URI.class));
    }

    private static class NumberToShortCoercion extends CoercionBase<Number, Short> {

        NumberToShortCoercion() {
            super(Number.class, Short.class);
        }

        @NotNull
        @Override
        public Short coerce(@NotNull Number value) {
            return value.shortValue();
        }
    }

    private static class NumberToLongCoercion extends CoercionBase<Number, Long> {

        NumberToLongCoercion() {
            super(Number.class, Long.class);
        }

        @NotNull
        @Override
        public Long coerce(@NotNull Number value) {
            return value.longValue();
        }
    }

    private static class NumberToFloatCoercion extends CoercionBase<Number, Float> {

        NumberToFloatCoercion() {
            super(Number.class, Float.class);
        }

        @NotNull
        @Override
        public Float coerce(@NotNull Number value) {
            return value.floatValue();
        }
    }

    private static class NumberToDoubleCoercion extends CoercionBase<Number, Double> {

        NumberToDoubleCoercion() {
            super(Number.class, Double.class);
        }

        @NotNull
        @Override
        public Double coerce(@NotNull Number value) {
            return value.doubleValue();
        }
    }

    private static class NumberToBigIntegerCoercion extends CoercionBase<Number, BigInteger> {

        NumberToBigIntegerCoercion() {
            super(Number.class, BigInteger.class);
        }

        @NotNull
        @Override
        public BigInteger coerce(@NotNull Number value) {
            return (value instanceof BigInteger) ? (BigInteger) value
                 : (value instanceof BigDecimal) ? ((BigDecimal) value).toBigInteger()
                 : (value instanceof Integer)    ? BigInteger.valueOf(value.intValue())
                 : (value instanceof Long)       ? BigInteger.valueOf(value.longValue())
                 : new BigInteger(value.toString());
        }
    }

    private static class NumberToBigDecimalCoercion extends CoercionBase<Number, BigDecimal> {

        NumberToBigDecimalCoercion() {
            super(Number.class, BigDecimal.class);
        }

        @NotNull
        @Override
        public BigDecimal coerce(@NotNull Number value) {
            return (value instanceof BigDecimal) ? (BigDecimal) value
                    : (value instanceof BigInteger) ? new BigDecimal((BigInteger) value)
                    : (value instanceof Integer)    ? BigDecimal.valueOf(value.intValue())
                    : (value instanceof Long)       ? BigDecimal.valueOf(value.longValue())
                    : new BigDecimal(value.toString());
        }
    }

    private static class BigIntegerToBigDecimalCoercion extends CoercionBase<BigInteger, BigDecimal> {

        BigIntegerToBigDecimalCoercion() {
            super(BigInteger.class, BigDecimal.class);
        }

        @NotNull
        @Override
        public BigDecimal coerce(@NotNull BigInteger value) {
            return new BigDecimal(value);
        }
    }

    private static class StringToUrlCoercion extends CoercionBase<String,URL> {

        StringToUrlCoercion() {
            super(String.class, URL.class);
        }

        @NotNull
        @Override
        public URL coerce(@NotNull String value) {
            try {
                return new URL(value);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    private static class ToStringCoercion<S> extends CoercionBase<S,String> {

        ToStringCoercion(Class<S> source) {
            super(source, String.class);
        }

        @NotNull
        @Override
        public String coerce(@NotNull S value) {
            return value.toString();
        }
    }

    private static class StringToUriCoercion extends CoercionBase<String,URI> {

        StringToUriCoercion() {
            super(String.class, URI.class);
        }

        @NotNull
        @Override
        public URI coerce(@NotNull String value) {
            try {
                return new URI(value);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
