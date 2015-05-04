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

package org.dalesbred.integration.threeten;

import org.dalesbred.conversion.TypeConversionRegistry;
import org.jetbrains.annotations.NotNull;
import org.threeten.bp.*;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Conversions for ThreeTen. These are automatically detected if ThreeTen is found on
 * classpath, so the user doesn't need to do anything to get ThreeTen-support.
 */
public final class ThreeTenTypeConversions {

    private static final long MILLIS_PER_SECOND = 1000;

    private static final int EPOCH_YEAR = 1900;

    private ThreeTenTypeConversions() {
    }

    /**
     * Returns true if java.time is found on classpath.
     */
    public static boolean hasThreeTen() {
        try {
            Class.forName("org.threeten.bp.LocalDate");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void register(@NotNull TypeConversionRegistry typeConversionRegistry) {
        typeConversionRegistry.registerNonNullConversions(Timestamp.class, Instant.class, ThreeTenTypeConversions::convertSqlTimeStampToInstant, ThreeTenTypeConversions::convertInstantToSqlTimestamp);
        typeConversionRegistry.registerNonNullConversions(Timestamp.class, LocalDateTime.class, ThreeTenTypeConversions::convertTimeStampToLocalDateTime, ThreeTenTypeConversions::convertLocalDateTimeToTimestamp);
        typeConversionRegistry.registerNonNullConversions(Time.class, LocalTime.class, ThreeTenTypeConversions::convertSqlTimeToLocalTime, ThreeTenTypeConversions::convertLocalTimeToSqlTime);
        typeConversionRegistry.registerNonNullConversions(String.class, ZoneId.class, ZoneId::of, ZoneId::getId);

        typeConversionRegistry.registerNonNullConversionFromDatabaseType(java.util.Date.class, LocalDate.class, ThreeTenTypeConversions::convertDateToLocalDate);
        typeConversionRegistry.registerNonNullConversionToDatabaseType(LocalDate.class, Date.class, ThreeTenTypeConversions::convertLocalDateToSqlDate);
    }

    @NotNull
    private static LocalDateTime convertTimeStampToLocalDateTime(@NotNull Timestamp value) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(value.getTime()), ZoneId.systemDefault());
    }

    @NotNull
    @SuppressWarnings("deprecation")
    private static Timestamp convertLocalDateTimeToTimestamp(@NotNull LocalDateTime value) {
        return new Timestamp(value.getYear() - EPOCH_YEAR,
                value.getMonthValue() - 1,
                value.getDayOfMonth(),
                value.getHour(),
                value.getMinute(),
                value.getSecond(),
                value.getNano());
    }

    @NotNull
    @SuppressWarnings("deprecation")
    private static LocalDate convertDateToLocalDate(@NotNull java.util.Date value) {
        return LocalDate.of(value.getYear() + EPOCH_YEAR, value.getMonth() + 1, value.getDate());
    }

    @SuppressWarnings("deprecation")
    @NotNull
    private static Date convertLocalDateToSqlDate(@NotNull LocalDate value) {
        return new Date(value.getYear() - EPOCH_YEAR, value.getMonthValue() - 1, value.getDayOfMonth());
    }

    @SuppressWarnings("deprecation")
    private static LocalTime convertSqlTimeToLocalTime(@NotNull Time value) {
        return LocalTime.of(value.getHours(), value.getMinutes(), value.getSeconds());
    }

    @NotNull
    @SuppressWarnings("deprecation")
    private static Time convertLocalTimeToSqlTime(@NotNull LocalTime value) {
        return new Time(value.getHour(), value.getMinute(), value.getSecond());
    }

    @NotNull
    private static Instant convertSqlTimeStampToInstant(@NotNull Timestamp value) {
        return Instant.ofEpochSecond(value.getTime() / MILLIS_PER_SECOND, value.getNanos());
    }

    @NotNull
    private static Timestamp convertInstantToSqlTimestamp(@NotNull Instant value) {
        try {
            Timestamp stamp = new Timestamp(value.getEpochSecond() * MILLIS_PER_SECOND);
            stamp.setNanos(value.getNano());
            return stamp;
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
