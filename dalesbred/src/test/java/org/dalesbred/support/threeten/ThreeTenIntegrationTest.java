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

package org.dalesbred.support.threeten;

import org.dalesbred.Database;
import org.dalesbred.Reflective;
import org.dalesbred.TestDatabaseProvider;
import org.dalesbred.TransactionalTestsRule;
import org.junit.Rule;
import org.junit.Test;
import org.threeten.bp.*;

import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.threeten.bp.temporal.ChronoUnit.SECONDS;

public class ThreeTenIntegrationTest {

    private final Database db = TestDatabaseProvider.createInMemoryHSQLDatabase();

    @Rule
    public final TransactionalTestsRule rule = new TransactionalTestsRule(db);

    @Test
    public void fetchLocalDateTime() {
        assertThat(db.findUnique(LocalDateTime.class, "VALUES (cast('2012-10-09 11:29:25' AS TIMESTAMP))"), is(LocalDateTime.of(2012, 10, 9, 11, 29, 25)));
    }

    @Test
    public void fetchInstant() {
        TimeZone oldDefault = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            Instant time = Instant.ofEpochMilli(1295000000000L);
            assertThat(db.findUnique(Instant.class, "VALUES (cast('2011-01-14 10:13:20' AS TIMESTAMP))"), is(time));
        } finally {
            TimeZone.setDefault(oldDefault);
        }
    }

    @Test
    public void storeInstant() {
        db.update("DROP TABLE IF EXISTS instant_test");
        db.update("CREATE TABLE instant_test (timestamp TIMESTAMP)");

        Instant instant = Instant.now();

        db.update("INSERT INTO instant_test (timestamp) VALUES (?)", instant);

        assertThat(db.findUnique(Instant.class, "SELECT timestamp FROM instant_test"), is(instant));
    }

    @Test
    public void fetchLocalDates() {
        assertThat(db.findUnique(LocalDate.class, "VALUES (cast('2012-10-09' AS DATE))"), is(LocalDate.of(2012, 10, 9)));
    }

    @Test
    public void fetchLocalTime() {
        assertThat(db.findUnique(LocalTime.class, "VALUES (cast('11:29:25' AS TIME))"), is(LocalTime.of(11, 29, 25)));
    }

    @Test
    public void localDatesWithTimeZoneProblems() {
        TimeZone oldDefault = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            assertThat(db.findUnique(LocalDate.class, "VALUES (cast('2012-10-09' AS DATE))"), is(LocalDate.of(2012, 10, 9)));

        } finally {
            TimeZone.setDefault(oldDefault);
        }
    }

    @Test
    public void localDatesFromTimestampWithTimeZoneProblems() {
        TimeZone oldDefault = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            assertThat(db.findUnique(LocalDate.class, "VALUES (cast('2012-10-09 00:00:00' AS TIMESTAMP))"), is(LocalDate.of(2012, 10, 9)));

        } finally {
            TimeZone.setDefault(oldDefault);
        }
    }

    @Test
    public void timesTypesAsParameters() {
        DateContainer container = db.findUnique(DateContainer.class, "VALUES (cast('2012-10-09 11:29:25' AS TIMESTAMP), cast('2012-10-09' AS DATE), cast('11:29:25' AS TIME))");

        assertThat(container.dateTime, is(LocalDateTime.of(2012, 10, 9, 11, 29, 25)));
        assertThat(container.date, is(LocalDate.of(2012, 10, 9)));
        assertThat(container.time, is(LocalTime.of(11, 29, 25)));
    }

    @Test
    public void saveJavaTimeTypes() {
        db.update("DROP TABLE IF EXISTS date_test");
        db.update("CREATE TABLE date_test (timestamp TIMESTAMP, date DATE, time TIME)");

        LocalDateTime dateTime = LocalDateTime.now();
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now().truncatedTo(SECONDS);

        db.update("INSERT INTO date_test (timestamp, date, time) VALUES (?, ?, ?)", dateTime, date, time);

        assertThat(db.findUnique(LocalDateTime.class, "SELECT timestamp FROM date_test"), is(dateTime));
        assertThat(db.findUnique(LocalDate.class, "SELECT date FROM date_test"), is(date));
        assertThat(db.findUnique(LocalTime.class, "SELECT time FROM date_test"), is(time));
    }

    @Test
    public void timeZoneConversions() {
        db.update("DROP TABLE IF EXISTS timezones");
        db.update("CREATE TEMPORARY TABLE timezones (zone_id VARCHAR(64))");

        ZoneId helsinkiTimeZone = ZoneId.of("Europe/Helsinki");

        db.update("INSERT INTO timezones (zone_id) VALUES (?)", helsinkiTimeZone);

        assertThat(db.findUnique(ZoneId.class, "SELECT zone_id FROM timezones"), is(helsinkiTimeZone));
    }

    public static class DateContainer {
        final LocalDateTime dateTime;
        final LocalDate date;
        final LocalTime time;

        @Reflective
        public DateContainer(LocalDateTime dateTime, LocalDate date, LocalTime time) {
            this.dateTime = dateTime;
            this.date = date;
            this.time = time;
        }
    }
}
