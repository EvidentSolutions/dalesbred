/*
 * Copyright (c) 2017 Evident Solutions Oy
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

package org.dalesbred.integration.joda

import org.dalesbred.TestDatabaseProvider
import org.dalesbred.TransactionalTestsRule
import org.dalesbred.testutils.withUTCTimeZone
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class JodaIntegrationTest {

    private val db = TestDatabaseProvider.createInMemoryHSQLDatabase()

    @get:Rule
    val rule = TransactionalTestsRule(db)

    @Test
    fun fetchJodaDateTimes() {
        assertEquals(DateTime(2012, 10, 9, 11, 29, 25), db.findUnique(DateTime::class.java, "values (cast('2012-10-09 11:29:25' as timestamp))"))
    }

    @Test
    fun fetchJodaDates() {
        assertEquals(LocalDate(2012, 10, 9), db.findUnique(LocalDate::class.java, "values (cast('2012-10-09' as date))"))
    }

    @Test
    fun fetchJodaTime() {
        assertEquals(LocalTime(11, 29, 25), db.findUnique(LocalTime::class.java, "values (cast('11:29:25' as time))"))
    }

    @Test
    fun localDatesWithTimeZoneProblems() {
        withUTCTimeZone {
            assertEquals(LocalDate(2012, 10, 9), db.findUnique(LocalDate::class.java, "values (cast('2012-10-09' as date))"))
        }
    }

    @Test
    fun localDatesFromTimestampWithTimeZoneProblems() {
        withUTCTimeZone {
            assertEquals(LocalDate(2012, 10, 9), db.findUnique(LocalDate::class.java, "values (cast('2012-10-09 00:00:00' as timestamp))"))
        }
    }

    @Test
    fun jodaTypesAsParameters() {
        val container = db.findUnique(DateContainer::class.java, "values (cast('2012-10-09 11:29:25' as timestamp), cast('2012-10-09' as date), cast('11:29:25' as time))")

        assertEquals(DateTime(2012, 10, 9, 11, 29, 25), container.dateTime)
        assertEquals(LocalDate(2012, 10, 9), container.date)
        assertEquals(LocalTime(11, 29, 25), container.time)
    }

    @Test
    fun saveJodaTypes() {
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
    fun timeZoneConversions() {
        db.update("drop table if exists timezones")
        db.update("create temporary table timezones (zone_id varchar(64))")

        val helsinkiTimeZone = DateTimeZone.forID("Europe/Helsinki")

        db.update("insert into timezones (zone_id) values (?)", helsinkiTimeZone)

        assertEquals(helsinkiTimeZone, db.findUnique(DateTimeZone::class.java, "select zone_id from timezones"))
    }

    private fun LocalTime.withoutMillis() = this.minusMillis(this.millisOfSecond)

    class DateContainer(val dateTime: DateTime, val date: LocalDate, val time: LocalTime)
}
