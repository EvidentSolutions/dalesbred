package fi.evident.dalesbred.instantiation;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class JodaCoercions {

    public static void register(@NotNull Coercions coercions) {
        coercions.register(new DateTimeCoercion());
        coercions.register(new LocalDateCoercion());
        coercions.register(new LocalTimeCoercion());
    }

    private static class DateTimeCoercion extends Coercion<Timestamp, DateTime> {
        DateTimeCoercion() {
            super(Timestamp.class, DateTime.class);
        }

        @NotNull
        @Override
        public DateTime coerce(@NotNull Timestamp value) {
            return new DateTime(value);
        }
    }

    private static class LocalDateCoercion extends Coercion<Date, LocalDate> {
        LocalDateCoercion() {
            super(Date.class, LocalDate.class);
        }

        @NotNull
        @Override
        public LocalDate coerce(@NotNull Date value) {
            return new LocalDate(value);
        }
    }

    private static class LocalTimeCoercion extends Coercion<Time, LocalTime> {
        LocalTimeCoercion() {
            super(Time.class, LocalTime.class);
        }

        @NotNull
        @Override
        public LocalTime coerce(@NotNull Time value) {
            return new LocalTime(value);
        }
    }

    public static boolean hasJoda() {
        try {
            Class.forName("org.joda.time.LocalDate");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
