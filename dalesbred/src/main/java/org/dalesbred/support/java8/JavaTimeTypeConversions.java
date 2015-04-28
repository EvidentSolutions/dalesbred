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

package org.dalesbred.support.java8;

import org.dalesbred.instantiation.TypeConversion;
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

    private static class LocalDateTimeFromSqlTimestampTypeConversion extends TypeConversion<Timestamp, LocalDateTime> {
        LocalDateTimeFromSqlTimestampTypeConversion() {
            super(Timestamp.class, LocalDateTime.class);
        }

        @NotNull
        @Override
        public LocalDateTime convert(@NotNull Timestamp value) {
            return value.toLocalDateTime();
        }
    }

    private static class LocalDateTimeToSqlTimestampTypeConversion extends TypeConversion<LocalDateTime, Timestamp> {
        LocalDateTimeToSqlTimestampTypeConversion() {
            super(LocalDateTime.class, Timestamp.class);
        }

        @NotNull
        @Override
        public Timestamp convert(@NotNull LocalDateTime value) {
            return Timestamp.valueOf(value);
        }
    }

    private static class LocalDateFromDateTypeConversion extends TypeConversion<java.util.Date, LocalDate> {
        LocalDateFromDateTypeConversion() {
            super(java.util.Date.class, LocalDate.class);
        }

        @NotNull
        @Override
        @SuppressWarnings({"MagicNumber", "deprecation"})
        public LocalDate convert(@NotNull java.util.Date value) {
            return LocalDate.of(value.getYear() + 1900, value.getMonth() + 1, value.getDate());
        }
    }


    private static class LocalDateToSqlDateTypeConversion extends TypeConversion<LocalDate, Date> {
        LocalDateToSqlDateTypeConversion() {
            super(LocalDate.class, Date.class);
        }

        @NotNull
        @Override
        public Date convert(@NotNull LocalDate value) {
            return Date.valueOf(value);
        }
    }

    private static class LocalTimeFromSqlTimeTypeConversion extends TypeConversion<Time, LocalTime> {
        LocalTimeFromSqlTimeTypeConversion() {
            super(Time.class, LocalTime.class);
        }

        @NotNull
        @Override
        public LocalTime convert(@NotNull Time value) {
            return value.toLocalTime();
        }
    }

    private static class LocalTimeToSqlTimeTypeConversion extends TypeConversion<LocalTime, Time> {
        LocalTimeToSqlTimeTypeConversion() {
            super(LocalTime.class, Time.class);
        }

        @NotNull
        @Override
        public Time convert(@NotNull LocalTime value) {
            return Time.valueOf(value);
        }
    }

    private static class InstantFromSqlTimestampTypeConversion extends TypeConversion<Timestamp, Instant> {

        InstantFromSqlTimestampTypeConversion() {
            super(Timestamp.class, Instant.class);
        }

        @NotNull
        @Override
        public Instant convert(@NotNull Timestamp value) {
            return value.toInstant();
        }
    }

    private static class InstantToSqlTimestampTypeConversion extends TypeConversion<Instant, Timestamp> {

        InstantToSqlTimestampTypeConversion() {
            super(Instant.class, Timestamp.class);
        }

        @NotNull
        @Override
        public Timestamp convert(@NotNull Instant value) {
            return Timestamp.from(value);
        }
    }

    private static class ZoneIdFromStringTypeConversion extends TypeConversion<String, ZoneId> {
        ZoneIdFromStringTypeConversion() {
            super(String.class, ZoneId.class);
        }

        @NotNull
        @Override
        public ZoneId convert(@NotNull String value) {
            return ZoneId.of(value);
        }
    }

    private static class ZoneIdToStringTypeConversion extends TypeConversion<ZoneId, String> {
        ZoneIdToStringTypeConversion() {
            super(ZoneId.class, String.class);
        }

        @NotNull
        @Override
        public String convert(@NotNull ZoneId value) {
            return value.getId();
        }
    }
}
