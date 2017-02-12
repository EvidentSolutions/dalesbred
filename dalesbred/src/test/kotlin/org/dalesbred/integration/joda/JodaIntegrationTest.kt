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
import org.hamcrest.CoreMatchers.`is`
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test

class JodaIntegrationTest {

    private val db = TestDatabaseProvider.createInMemoryHSQLDatabase()

    @get:Rule
    val rule = TransactionalTestsRule(db)

    @Test
    fun fetchJodaDateTimes() {
        assertThat(db.findUnique(DateTime::class.java, "values (cast('2012-10-09 11:29:25' as timestamp))"), `is`(DateTime(2012, 10, 9, 11, 29, 25)))
    }

    @Test
    fun fetchJodaDates() {
        assertThat(db.findUnique(LocalDate::class.java, "values (cast('2012-10-09' as date))"), `is`(LocalDate(2012, 10, 9)))
    }

    @Test
    fun fetchJodaTime() {
        assertThat(db.findUnique(LocalTime::class.java, "values (cast('11:29:25' as time))"), `is`(LocalTime(11, 29, 25)))
    }

    @Test
    fun localDatesWithTimeZoneProblems() {
        val oldDefault = DateTimeZone.getDefault()
        try {
            DateTimeZone.setDefault(DateTimeZone.forID("UTC"))

            assertThat(db.findUnique(LocalDate::class.java, "values (cast('2012-10-09' as date))"), `is`(LocalDate(2012, 10, 9)))

        } finally {
            DateTimeZone.setDefault(oldDefault)
        }
    }

    @Test
    fun localDatesFromTimestampWithTimeZoneProblems() {
        val oldDefault = DateTimeZone.getDefault()
        try {
            DateTimeZone.setDefault(DateTimeZone.forID("UTC"))

            assertThat(db.findUnique(LocalDate::class.java, "values (cast('2012-10-09 00:00:00' as timestamp))"), `is`(LocalDate(2012, 10, 9)))

        } finally {
            DateTimeZone.setDefault(oldDefault)
        }
    }

    @Test
    fun jodaTypesAsParameters() {
        val container = db.findUnique(DateContainer::class.java, "values (cast('2012-10-09 11:29:25' as timestamp), cast('2012-10-09' as date), cast('11:29:25' as time))")

        assertThat(container.dateTime, `is`(DateTime(2012, 10, 9, 11, 29, 25)))
        assertThat(container.date, `is`(LocalDate(2012, 10, 9)))
        assertThat(container.time, `is`(LocalTime(11, 29, 25)))
    }

    @Test
    fun saveJodaTypes() {
        db.update("drop table if exists date_test")
        db.update("create table date_test (timestamp timestamp, date date, time time)")

        val dateTime = DateTime.now()
        val date = LocalDate.now()
        val time = withoutMillis(LocalTime.now())

        db.update("insert into date_test (timestamp, date, time) values (?, ?, ?)", dateTime, date, time)

        assertThat(db.findUnique(DateTime::class.java, "select timestamp from date_test"), `is`(dateTime))
        assertThat(db.findUnique(LocalDate::class.java, "select date from date_test"), `is`(date))
        assertThat(db.findUnique(LocalTime::class.java, "select time from date_test"), `is`(time))
    }

    @Test
    fun timeZoneConversions() {
        db.update("drop table if exists timezones")
        db.update("create temporary table timezones (zone_id varchar(64))")

        val helsinkiTimeZone = DateTimeZone.forID("Europe/Helsinki")

        db.update("insert into timezones (zone_id) values (?)", helsinkiTimeZone)

        assertThat(db.findUnique(DateTimeZone::class.java, "select zone_id from timezones"), `is`(helsinkiTimeZone))
    }

    private fun withoutMillis(time: LocalTime): LocalTime {
        return time.minusMillis(time.millisOfSecond)
    }

    class DateContainer(val dateTime: DateTime, val date: LocalDate, val time: LocalTime)
}
