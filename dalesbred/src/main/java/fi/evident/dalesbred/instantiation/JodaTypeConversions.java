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
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

final class JodaTypeConversions {

    private JodaTypeConversions() { }

    public static boolean hasJoda() {
        try {
            Class.forName("org.joda.time.LocalDate");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void register(@NotNull TypeConversionRegistry typeConversionRegistry) {
        typeConversionRegistry.registerLoadConversion(new DateTimeFromSqlTimestampTypeConversion());
        typeConversionRegistry.registerLoadConversion(new LocalDateFromSqlDateTypeConversion());
        typeConversionRegistry.registerLoadConversion(new LocalTimeFromSqlTimeTypeConversion());

        typeConversionRegistry.registerStoreConversion(new DateTimeToSqlTimestampTypeConversion());
        typeConversionRegistry.registerStoreConversion(new LocalDateToSqlDateTypeConversion());
        typeConversionRegistry.registerStoreConversion(new LocalTimeToSqlTimeTypeConversion());
    }

    private static class DateTimeFromSqlTimestampTypeConversion extends TypeConversionBase<Timestamp, DateTime> {
        DateTimeFromSqlTimestampTypeConversion() {
            super(Timestamp.class, DateTime.class);
        }

        @NotNull
        @Override
        public DateTime convert(@NotNull Timestamp value) {
            return new DateTime(value);
        }
    }

    private static class DateTimeToSqlTimestampTypeConversion extends TypeConversionBase<DateTime,Timestamp> {
        DateTimeToSqlTimestampTypeConversion() {
            super(DateTime.class, Timestamp.class);
        }

        @NotNull
        @Override
        public Timestamp convert(@NotNull DateTime value) {
            return new Timestamp(value.getMillis());
        }
    }

    private static class LocalDateFromSqlDateTypeConversion extends TypeConversionBase<Date, LocalDate> {
        LocalDateFromSqlDateTypeConversion() {
            super(Date.class, LocalDate.class);
        }

        @NotNull
        @Override
        public LocalDate convert(@NotNull Date value) {
            return new LocalDate(value);
        }
    }


    private static class LocalDateToSqlDateTypeConversion extends TypeConversionBase<LocalDate, Date> {
        LocalDateToSqlDateTypeConversion() {
            super(LocalDate.class, Date.class);
        }

        @NotNull
        @Override
        public Date convert(@NotNull LocalDate value) {
            return new Date(value.toDateTimeAtStartOfDay().getMillis());
        }
    }

    private static class LocalTimeFromSqlTimeTypeConversion extends TypeConversionBase<Time, LocalTime> {
        LocalTimeFromSqlTimeTypeConversion() {
            super(Time.class, LocalTime.class);
        }

        @NotNull
        @Override
        public LocalTime convert(@NotNull Time value) {
            return new LocalTime(value);
        }
    }

    private static class LocalTimeToSqlTimeTypeConversion extends TypeConversionBase<LocalTime, Time> {
        LocalTimeToSqlTimeTypeConversion() {
            super(LocalTime.class, Time.class);
        }

        @NotNull
        @Override
        public Time convert(@NotNull LocalTime value) {
            return new Time(value.toDateTimeToday(DateTimeZone.getDefault()).getMillis());
        }
    }
}
