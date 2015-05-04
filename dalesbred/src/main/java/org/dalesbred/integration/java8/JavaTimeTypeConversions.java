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

import org.dalesbred.conversion.TypeConversionRegistry;
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

    private static final int EPOCH_YEAR = 1900;

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
        typeConversionRegistry.registerNonNullConversions(Timestamp.class, Instant.class, Timestamp::toInstant, Timestamp::from);
        typeConversionRegistry.registerNonNullConversions(Timestamp.class, LocalDateTime.class, Timestamp::toLocalDateTime, Timestamp::valueOf);
        typeConversionRegistry.registerNonNullConversions(Time.class, LocalTime.class, Time::toLocalTime, Time::valueOf);
        typeConversionRegistry.registerNonNullConversions(String.class, ZoneId.class, ZoneId::of, ZoneId::getId);

        typeConversionRegistry.registerNonNullConversionFromDatabaseType(java.util.Date.class, LocalDate.class, JavaTimeTypeConversions::convertDateToLocalDate);
        typeConversionRegistry.registerNonNullConversionToDatabaseType(LocalDate.class, Date.class, Date::valueOf);
    }

    @NotNull
    @SuppressWarnings("deprecation")
    private static LocalDate convertDateToLocalDate(@NotNull java.util.Date value) {
        return LocalDate.of(value.getYear() + EPOCH_YEAR, value.getMonth() + 1, value.getDate());
    }
}
