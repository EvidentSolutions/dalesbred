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

import org.dalesbred.conversion.TypeConversionRegistry;
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
        typeConversionRegistry.registerConversions(Timestamp.class, DateTime.class, DateTime::new, v -> new Timestamp(v.getMillis()));

        typeConversionRegistry.registerConversionFromDatabase(java.util.Date.class, LocalDate.class, LocalDate::fromDateFields);
        typeConversionRegistry.registerConversionFromDatabase(Time.class, LocalTime.class, LocalTime::new);
        typeConversionRegistry.registerConversionFromDatabase(String.class, DateTimeZone.class, DateTimeZone::forID);

        typeConversionRegistry.registerConversionToDatabase(LocalDate.class, Date.class, value -> new Date(value.toDateTimeAtStartOfDay().getMillis()));
        typeConversionRegistry.registerConversionToDatabase(LocalTime.class, Time.class, value -> new Time(value.toDateTimeToday(DateTimeZone.getDefault()).getMillis()));
        typeConversionRegistry.registerConversionToDatabase(DateTimeZone.class, String.class, DateTimeZone::getID);
    }
}
