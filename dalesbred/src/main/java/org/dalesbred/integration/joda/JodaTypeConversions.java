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
    @SuppressWarnings("Java9ReflectionClassVisibility")
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

        typeConversionRegistry.registerConversionToDatabase(LocalDate.class, value -> new Date(value.toDateTimeAtStartOfDay().getMillis()));
        typeConversionRegistry.registerConversionToDatabase(LocalTime.class, value -> new Time(value.toDateTimeToday(DateTimeZone.getDefault()).getMillis()));
        typeConversionRegistry.registerConversionToDatabase(DateTimeZone.class, DateTimeZone::getID);
    }
}
