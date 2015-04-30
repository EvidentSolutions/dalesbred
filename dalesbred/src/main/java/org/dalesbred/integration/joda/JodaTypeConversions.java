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

package org.dalesbred.integration.joda;

import org.dalesbred.instantiation.SimpleNonNullTypeConversion;
import org.dalesbred.instantiation.TypeConversionRegistry;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Conversions for Joda Time. These are automatically detected if Joda is found on
 * classpath, so the user doesn't need to do anything to get Joda-support.
 */
public final class JodaTypeConversions {

    private JodaTypeConversions() { }

    /**
     * Returns true if Joda is found on classpath.
     */
    public static boolean hasJoda() {
        try {
            Class.forName("org.joda.time.LocalDate");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void register(@NotNull TypeConversionRegistry typeConversionRegistry) {
        typeConversionRegistry.registerConversionFromDatabaseType(new DateTimeFromSqlTimestampTypeConversion());
        typeConversionRegistry.registerConversionFromDatabaseType(new LocalDateFromDateTypeConversion());
        typeConversionRegistry.registerConversionFromDatabaseType(new LocalTimeFromSqlTimeTypeConversion());
        typeConversionRegistry.registerConversionFromDatabaseType(new DateTimeZoneFromStringTypeConversion());

        typeConversionRegistry.registerConversionToDatabaseType(new DateTimeToSqlTimestampTypeConversion());
        typeConversionRegistry.registerConversionToDatabaseType(new LocalDateToSqlDateTypeConversion());
        typeConversionRegistry.registerConversionToDatabaseType(new LocalTimeToSqlTimeTypeConversion());
        typeConversionRegistry.registerConversionToDatabaseType(new DateTimeToStringTypeConversion());
    }

    private static class DateTimeFromSqlTimestampTypeConversion extends SimpleNonNullTypeConversion<Timestamp, DateTime> {
        DateTimeFromSqlTimestampTypeConversion() {
            super(Timestamp.class, DateTime.class);
        }

        @NotNull
        @Override
        public DateTime convertNonNull(@NotNull Timestamp value) {
            return new DateTime(value);
        }
    }

    private static class DateTimeToSqlTimestampTypeConversion extends SimpleNonNullTypeConversion<DateTime,Timestamp> {
        DateTimeToSqlTimestampTypeConversion() {
            super(DateTime.class, Timestamp.class);
        }

        @NotNull
        @Override
        public Timestamp convertNonNull(@NotNull DateTime value) {
            return new Timestamp(value.getMillis());
        }
    }

    private static class LocalDateFromDateTypeConversion extends SimpleNonNullTypeConversion<java.util.Date, LocalDate> {
        LocalDateFromDateTypeConversion() {
            super(java.util.Date.class, LocalDate.class);
        }

        @NotNull
        @Override
        public LocalDate convertNonNull(@NotNull java.util.Date value) {
            return LocalDate.fromDateFields(value);
        }
    }


    private static class LocalDateToSqlDateTypeConversion extends SimpleNonNullTypeConversion<LocalDate, Date> {
        LocalDateToSqlDateTypeConversion() {
            super(LocalDate.class, Date.class);
        }

        @NotNull
        @Override
        public Date convertNonNull(@NotNull LocalDate value) {
            return new Date(value.toDateTimeAtStartOfDay().getMillis());
        }
    }

    private static class LocalTimeFromSqlTimeTypeConversion extends SimpleNonNullTypeConversion<Time, LocalTime> {
        LocalTimeFromSqlTimeTypeConversion() {
            super(Time.class, LocalTime.class);
        }

        @NotNull
        @Override
        public LocalTime convertNonNull(@NotNull Time value) {
            return new LocalTime(value);
        }
    }

    private static class LocalTimeToSqlTimeTypeConversion extends SimpleNonNullTypeConversion<LocalTime, Time> {
        LocalTimeToSqlTimeTypeConversion() {
            super(LocalTime.class, Time.class);
        }

        @NotNull
        @Override
        public Time convertNonNull(@NotNull LocalTime value) {
            return new Time(value.toDateTimeToday(DateTimeZone.getDefault()).getMillis());
        }
    }

    private static class DateTimeZoneFromStringTypeConversion extends SimpleNonNullTypeConversion<String, DateTimeZone> {
        DateTimeZoneFromStringTypeConversion() {
            super(String.class, DateTimeZone.class);
        }

        @NotNull
        @Override
        public DateTimeZone convertNonNull(@NotNull String value) {
            return DateTimeZone.forID(value);
        }
    }

    private static class DateTimeToStringTypeConversion extends SimpleNonNullTypeConversion<DateTimeZone, String> {
        DateTimeToStringTypeConversion() {
            super(DateTimeZone.class, String.class);
        }

        @NotNull
        @Override
        public String convertNonNull(@NotNull DateTimeZone value) {
            return value.getID();
        }
    }
}
