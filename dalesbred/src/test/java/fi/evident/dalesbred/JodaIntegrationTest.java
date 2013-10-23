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

package fi.evident.dalesbred;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JodaIntegrationTest {

    private final Database db = TestDatabaseProvider.createInMemoryHSQLDatabase();

    @Rule
    public final TransactionalTestsRule rule = new TransactionalTestsRule(db);

    @Test
    public void fetchJodaDateTimes() {
        assertThat(db.findUnique(DateTime.class, "values (cast('2012-10-09 11:29:25' as timestamp))"), is(new DateTime(2012, 10, 9, 11, 29, 25)));
    }

    @Test
    public void fetchJodaDates() {
        assertThat(db.findUnique(LocalDate.class, "values (cast('2012-10-09' as date))"), is(new LocalDate(2012, 10, 9)));
    }

    @Test
    public void fetchJodaTime() {
        assertThat(db.findUnique(LocalTime.class, "values (cast('11:29:25' as time))"), is(new LocalTime(11, 29, 25)));
    }

    @Test
    public void localDatesWithTimeZoneProblems() {
        DateTimeZone oldDefault = DateTimeZone.getDefault();
        try {
            DateTimeZone.setDefault(DateTimeZone.forID("UTC"));

            assertThat(db.findUnique(LocalDate.class, "values (cast('2012-10-09' as date))"), is(new LocalDate(2012, 10, 9)));

        } finally {
            DateTimeZone.setDefault(oldDefault);
        }
    }

    @Test
    public void localDatesFromTimestampWithTimeZoneProblems() {
        DateTimeZone oldDefault = DateTimeZone.getDefault();
        try {
            DateTimeZone.setDefault(DateTimeZone.forID("UTC"));

            assertThat(db.findUnique(LocalDate.class, "values (cast('2012-10-09 00:00:00' as timestamp))"), is(new LocalDate(2012, 10, 9)));

        } finally {
            DateTimeZone.setDefault(oldDefault);
        }
    }

    @Test
    public void jodaTypesAsParameters() {
        DateContainer container = db.findUnique(DateContainer.class, "values (cast('2012-10-09 11:29:25' as timestamp), cast('2012-10-09' as date), cast('11:29:25' as time))");

        assertThat(container.dateTime, is(new DateTime(2012, 10, 9, 11, 29, 25)));
        assertThat(container.date, is(new LocalDate(2012, 10, 9)));
        assertThat(container.time, is(new LocalTime(11, 29, 25)));
    }

    @Test
    public void saveJodaTypes() {
        db.update("drop table if exists date_test");
        db.update("create table date_test (timestamp timestamp, date date, time time)");

        DateTime dateTime = DateTime.now();
        LocalDate date = LocalDate.now();
        LocalTime time = withoutMillis(LocalTime.now());

        db.update("insert into date_test (timestamp, date, time) values (?, ?, ?)", dateTime, date, time);

        assertThat(db.findUnique(DateTime.class, "select timestamp from date_test"), is(dateTime));
        assertThat(db.findUnique(LocalDate.class, "select date from date_test"), is(date));
        assertThat(db.findUnique(LocalTime.class, "select time from date_test"), is(time));
    }

    @Test
    public void timeZoneConversions() {
        db.update("drop table if exists timezones");
        db.update("create temporary table timezones (zone_id varchar(64))");

        DateTimeZone helsinkiTimeZone = DateTimeZone.forID("Europe/Helsinki");

        db.update("insert into timezones (zone_id) values (?)", helsinkiTimeZone);

        assertThat(db.findUnique(DateTimeZone.class, "select zone_id from timezones"), is(helsinkiTimeZone));
    }

    @NotNull
    private static LocalTime withoutMillis(@NotNull LocalTime time) {
        return time.minusMillis(time.getMillisOfSecond());
    }

    public static class DateContainer {
        final DateTime dateTime;
        final LocalDate date;
        final LocalTime time;

        @Reflective
        public DateContainer(DateTime dateTime, LocalDate date, LocalTime time) {
            this.dateTime = dateTime;
            this.date = date;
            this.time = time;
        }
    }
}
