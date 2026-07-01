@file:Suppress("SqlResolve")

package org.dalesbred.integration.joda

import org.dalesbred.Database
import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import org.dalesbred.testutils.transactionalTest
import org.dalesbred.testutils.withUTCTimeZone
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

@DatabaseTest(POSTGRESQL)
class JodaIntegrationTest(private val db: Database) {

    @Test
    fun `fetch joda DateTimes`() = transactionalTest(db) {
        assertEquals(DateTime(2012, 10, 9, 11, 29, 25), db.findUnique(DateTime::class.java, "values (cast('2012-10-09 11:29:25' as timestamp))"))
    }

    @Test
    fun `fetch joda LocalDates`() = transactionalTest(db) {
        assertEquals(LocalDate(2012, 10, 9), db.findUnique(LocalDate::class.java, "values (cast('2012-10-09' as date))"))
    }

    @Test
    fun `fetch joda LocalTime`() = transactionalTest(db) {
        assertEquals(LocalTime(11, 29, 25), db.findUnique(LocalTime::class.java, "values (cast('11:29:25' as time))"))
    }

    @Test
    fun `LocalDates with time-zone problems`() = transactionalTest(db) {
        withUTCTimeZone {
            assertEquals(LocalDate(2012, 10, 9), db.findUnique(LocalDate::class.java, "values (cast('2012-10-09' as date))"))
        }
    }

    @Test
    fun `LocalDates from timestamp with time-zone problems`() = transactionalTest(db) {
        withUTCTimeZone {
            assertEquals(LocalDate(2012, 10, 9), db.findUnique(LocalDate::class.java, "values (cast('2012-10-09 00:00:00' as timestamp))"))
        }
    }

    @Test
    fun `joda types as parameters`() = transactionalTest(db) {
        val container = db.findUnique(DateContainer::class.java, "values (cast('2012-10-09 11:29:25' as timestamp), cast('2012-10-09' as date), cast('11:29:25' as time))")

        assertEquals(DateTime(2012, 10, 9, 11, 29, 25), container.dateTime)
        assertEquals(LocalDate(2012, 10, 9), container.date)
        assertEquals(LocalTime(11, 29, 25), container.time)
    }

    @Test
    fun `save joda types`() = transactionalTest(db) {
        db.update("drop table if exists date_test")
        db.update("create table date_test (timestamp timestamp, date date, time time)")

        val dateTime = DateTime.now()
        val date = LocalDate.now()
        val time = LocalTime.now().withoutMillis()

        db.update("insert into date_test (timestamp, date, time) values (?, ?, ?)", dateTime, date, time)

        assertEquals(dateTime, db.findUnique(DateTime::class.java, "select timestamp from date_test"))
        assertEquals(date, db.findUnique(LocalDate::class.java, "select date from date_test"))
        assertEquals(time, db.findUnique(LocalTime::class.java, "select time from date_test"))
    }

    @Test
    fun `time-zone conversions`() = transactionalTest(db) {
        db.update("drop table if exists timezones")
        db.update("create temporary table timezones (zone_id varchar(64))")

        val helsinkiTimeZone = DateTimeZone.forID("Europe/Helsinki")

        db.update("insert into timezones (zone_id) values (?)", helsinkiTimeZone)

        assertEquals(helsinkiTimeZone, db.findUnique(DateTimeZone::class.java, "select zone_id from timezones"))
    }

    private fun LocalTime.withoutMillis() = this.minusMillis(this.millisOfSecond)

    class DateContainer(val dateTime: DateTime, val date: LocalDate, val time: LocalTime)
}
