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

package org.dalesbred.integration.java8;

import org.dalesbred.instantiation.SimpleNonNullTypeConversion;
import org.dalesbred.instantiation.TypeConversionRegistry;
import org.jetbrains.annotations.NotNull;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;

/**
 * Conversions for java.time. These are automatically detected if java.time is found on
 * classpath, so the user doesn't need to do anything to get java.time-support.
 */
public final class JavaTimeTypeConversions {

    private JavaTimeTypeConversions() {
    }

    /**
     * Returns true if java.time is found on classpath.
     */
    public static boolean hasJavaTime() {
        try {
            Class.forName("java.time.LocalDate");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void register(@NotNull TypeConversionRegistry typeConversionRegistry) {
        typeConversionRegistry.registerConversionFromDatabaseType(new InstantFromSqlTimestampTypeConversion());
        typeConversionRegistry.registerConversionFromDatabaseType(new LocalDateTimeFromSqlTimestampTypeConversion());
        typeConversionRegistry.registerConversionFromDatabaseType(new LocalDateFromDateTypeConversion());
        typeConversionRegistry.registerConversionFromDatabaseType(new LocalTimeFromSqlTimeTypeConversion());
        typeConversionRegistry.registerConversionFromDatabaseType(new ZoneIdFromStringTypeConversion());

        typeConversionRegistry.registerConversionToDatabaseType(new InstantToSqlTimestampTypeConversion());
        typeConversionRegistry.registerConversionToDatabaseType(new LocalDateTimeToSqlTimestampTypeConversion());
        typeConversionRegistry.registerConversionToDatabaseType(new LocalDateToSqlDateTypeConversion());
        typeConversionRegistry.registerConversionToDatabaseType(new LocalTimeToSqlTimeTypeConversion());
        typeConversionRegistry.registerConversionToDatabaseType(new ZoneIdToStringTypeConversion());
    }

    private static class LocalDateTimeFromSqlTimestampTypeConversion extends SimpleNonNullTypeConversion<Timestamp, LocalDateTime> {
        LocalDateTimeFromSqlTimestampTypeConversion() {
            super(Timestamp.class, LocalDateTime.class);
        }

        @NotNull
        @Override
        public LocalDateTime convertNonNull(@NotNull Timestamp value) {
            return value.toLocalDateTime();
        }
    }

    private static class LocalDateTimeToSqlTimestampTypeConversion extends SimpleNonNullTypeConversion<LocalDateTime, Timestamp> {
        LocalDateTimeToSqlTimestampTypeConversion() {
            super(LocalDateTime.class, Timestamp.class);
        }

        @NotNull
        @Override
        public Timestamp convertNonNull(@NotNull LocalDateTime value) {
            return Timestamp.valueOf(value);
        }
    }

    private static class LocalDateFromDateTypeConversion extends SimpleNonNullTypeConversion<java.util.Date, LocalDate> {
        LocalDateFromDateTypeConversion() {
            super(java.util.Date.class, LocalDate.class);
        }

        @NotNull
        @Override
        @SuppressWarnings({"MagicNumber", "deprecation"})
        public LocalDate convertNonNull(@NotNull java.util.Date value) {
            return LocalDate.of(value.getYear() + 1900, value.getMonth() + 1, value.getDate());
        }
    }


    private static class LocalDateToSqlDateTypeConversion extends SimpleNonNullTypeConversion<LocalDate, Date> {
        LocalDateToSqlDateTypeConversion() {
            super(LocalDate.class, Date.class);
        }

        @NotNull
        @Override
        public Date convertNonNull(@NotNull LocalDate value) {
            return Date.valueOf(value);
        }
    }

    private static class LocalTimeFromSqlTimeTypeConversion extends SimpleNonNullTypeConversion<Time, LocalTime> {
        LocalTimeFromSqlTimeTypeConversion() {
            super(Time.class, LocalTime.class);
        }

        @NotNull
        @Override
        public LocalTime convertNonNull(@NotNull Time value) {
            return value.toLocalTime();
        }
    }

    private static class LocalTimeToSqlTimeTypeConversion extends SimpleNonNullTypeConversion<LocalTime, Time> {
        LocalTimeToSqlTimeTypeConversion() {
            super(LocalTime.class, Time.class);
        }

        @NotNull
        @Override
        public Time convertNonNull(@NotNull LocalTime value) {
            return Time.valueOf(value);
        }
    }

    private static class InstantFromSqlTimestampTypeConversion extends SimpleNonNullTypeConversion<Timestamp, Instant> {

        InstantFromSqlTimestampTypeConversion() {
            super(Timestamp.class, Instant.class);
        }

        @NotNull
        @Override
        public Instant convertNonNull(@NotNull Timestamp value) {
            return value.toInstant();
        }
    }

    private static class InstantToSqlTimestampTypeConversion extends SimpleNonNullTypeConversion<Instant, Timestamp> {

        InstantToSqlTimestampTypeConversion() {
            super(Instant.class, Timestamp.class);
        }

        @NotNull
        @Override
        public Timestamp convertNonNull(@NotNull Instant value) {
            return Timestamp.from(value);
        }
    }

    private static class ZoneIdFromStringTypeConversion extends SimpleNonNullTypeConversion<String, ZoneId> {
        ZoneIdFromStringTypeConversion() {
            super(String.class, ZoneId.class);
        }

        @NotNull
        @Override
        public ZoneId convertNonNull(@NotNull String value) {
            return ZoneId.of(value);
        }
    }

    private static class ZoneIdToStringTypeConversion extends SimpleNonNullTypeConversion<ZoneId, String> {
        ZoneIdToStringTypeConversion() {
            super(ZoneId.class, String.class);
        }

        @NotNull
        @Override
        public String convertNonNull(@NotNull ZoneId value) {
            return value.getId();
        }
    }
}
