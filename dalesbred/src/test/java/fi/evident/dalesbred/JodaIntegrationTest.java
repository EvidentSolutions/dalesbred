package fi.evident.dalesbred;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JodaIntegrationTest {

    private final Database db = TestDatabaseProvider.createTestDatabase();

    @Rule
    public final TransactionalTestsRule rule = new TransactionalTestsRule(db);

    @Test
    public void fetchJodaDateTimes() {
        assertThat(db.findUnique(DateTime.class, "select '2012-10-09 11:29:25'::timestamp"), is(new DateTime(2012, 10, 9, 11, 29, 25)));
    }

    @Test
    public void fetchJodaDates() {
        assertThat(db.findUnique(LocalDate.class, "select '2012-10-09'::date"), is(new LocalDate(2012, 10, 9)));
    }

    @Test
    public void fetchJodaTime() {
        assertThat(db.findUnique(LocalTime.class, "select '11:29:25'::time"), is(new LocalTime(11, 29, 25)));
    }

    @Test
    public void jodaTypesAsParameters() {
        DateContainer container = db.findUnique(DateContainer.class, "select '2012-10-09 11:29:25'::timestamp, '2012-10-09'::date, '11:29:25'::time");

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
        LocalTime time = LocalTime.now();

        db.update("insert into date_test (timestamp, date, time) values (?, ?, ?)", dateTime, date, time);

        assertThat(db.findUnique(DateTime.class, "select timestamp from date_test"), is(dateTime));
        assertThat(db.findUnique(LocalDate.class, "select date from date_test"), is(date));
        assertThat(db.findUnique(LocalTime.class, "select time from date_test"), is(time));
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
