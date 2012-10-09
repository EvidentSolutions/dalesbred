package fi.evident.dalesbred.instantiation;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public final class JodaCoercions {

    private JodaCoercions() { }

    public static boolean hasJoda() {
        try {
            Class.forName("org.joda.time.LocalDate");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void register(@NotNull Coercions coercions) {
        coercions.registerLoadConversion(new DateTimeFromSqlTimestampCoercion());
        coercions.registerLoadConversion(new LocalDateFromSqlDateCoercion());
        coercions.registerLoadConversion(new LocalTimeFromSqlTimeCoercion());

        coercions.registerStoreConversion(new DateTimeToSqlTimestampCoercion());
        coercions.registerStoreConversion(new LocalDateToSqlDateCoercion());
        coercions.registerStoreConversion(new LocalTimeToSqlTimeCoercion());
    }

    private static class DateTimeFromSqlTimestampCoercion extends CoercionBase<Timestamp, DateTime> {
        DateTimeFromSqlTimestampCoercion() {
            super(Timestamp.class, DateTime.class);
        }

        @NotNull
        @Override
        public DateTime coerce(@NotNull Timestamp value) {
            return new DateTime(value);
        }
    }

    private static class DateTimeToSqlTimestampCoercion extends CoercionBase<DateTime,Timestamp> {
        DateTimeToSqlTimestampCoercion() {
            super(DateTime.class, Timestamp.class);
        }

        @NotNull
        @Override
        public Timestamp coerce(@NotNull DateTime value) {
            return new Timestamp(value.getMillis());
        }
    }

    private static class LocalDateFromSqlDateCoercion extends CoercionBase<Date, LocalDate> {
        LocalDateFromSqlDateCoercion() {
            super(Date.class, LocalDate.class);
        }

        @NotNull
        @Override
        public LocalDate coerce(@NotNull Date value) {
            return new LocalDate(value);
        }
    }


    private static class LocalDateToSqlDateCoercion extends CoercionBase<LocalDate, Date> {
        LocalDateToSqlDateCoercion() {
            super(LocalDate.class, Date.class);
        }

        @NotNull
        @Override
        public Date coerce(@NotNull LocalDate value) {
            return new Date(value.toDateTimeAtStartOfDay().getMillis());
        }
    }

    private static class LocalTimeFromSqlTimeCoercion extends CoercionBase<Time, LocalTime> {
        LocalTimeFromSqlTimeCoercion() {
            super(Time.class, LocalTime.class);
        }

        @NotNull
        @Override
        public LocalTime coerce(@NotNull Time value) {
            return new LocalTime(value);
        }
    }

    private static class LocalTimeToSqlTimeCoercion extends CoercionBase<LocalTime, Time> {
        LocalTimeToSqlTimeCoercion() {
            super(LocalTime.class, Time.class);
        }

        @NotNull
        @Override
        public Time coerce(@NotNull LocalTime value) {
            return new Time(value.toDateTimeToday(DateTimeZone.getDefault()).getMillis());
        }
    }
}
