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

package org.dalesbred.integration.threeten

import org.dalesbred.TestDatabaseProvider
import org.dalesbred.TransactionalTestsRule
import org.dalesbred.annotation.Reflective
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.*
import org.threeten.bp.temporal.ChronoUnit.SECONDS
import java.util.*

class ThreeTenIntegrationTest {

    private val db = TestDatabaseProvider.createInMemoryHSQLDatabase()

    @get:Rule val rule = TransactionalTestsRule(db)

    @Test
    fun fetchLocalDateTime() {
        assertThat(db.findUnique(LocalDateTime::class.java, "VALUES (cast('2012-10-09 11:29:25' AS TIMESTAMP))"), `is`(LocalDateTime.of(2012, 10, 9, 11, 29, 25)))
    }

    @Test
    fun fetchInstant() {
        val oldDefault = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            val time = Instant.ofEpochMilli(1295000000000L)
            assertThat(db.findUnique(Instant::class.java, "VALUES (cast('2011-01-14 10:13:20' AS TIMESTAMP))"), `is`(time))
        } finally {
            TimeZone.setDefault(oldDefault)
        }
    }

    @Test
    fun storeInstant() {
        db.update("DROP TABLE IF EXISTS instant_test")
        db.update("CREATE TABLE instant_test (timestamp TIMESTAMP)")

        val instant = Instant.now()

        db.update("INSERT INTO instant_test (timestamp) VALUES (?)", instant)

        assertThat(db.findUnique(Instant::class.java, "SELECT timestamp FROM instant_test"), `is`(instant))
    }

    @Test
    fun fetchLocalDates() {
        assertThat(db.findUnique(LocalDate::class.java, "VALUES (cast('2012-10-09' AS DATE))"), `is`(LocalDate.of(2012, 10, 9)))
    }

    @Test
    fun fetchLocalTime() {
        assertThat(db.findUnique(LocalTime::class.java, "VALUES (cast('11:29:25' AS TIME))"), `is`(LocalTime.of(11, 29, 25)))
    }

    @Test
    fun localDatesWithTimeZoneProblems() {
        val oldDefault = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

            assertThat(db.findUnique(LocalDate::class.java, "VALUES (cast('2012-10-09' AS DATE))"), `is`(LocalDate.of(2012, 10, 9)))

        } finally {
            TimeZone.setDefault(oldDefault)
        }
    }

    @Test
    fun localDatesFromTimestampWithTimeZoneProblems() {
        val oldDefault = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

            assertThat(db.findUnique(LocalDate::class.java, "VALUES (cast('2012-10-09 00:00:00' AS TIMESTAMP))"), `is`(LocalDate.of(2012, 10, 9)))

        } finally {
            TimeZone.setDefault(oldDefault)
        }
    }

    @Test
    fun timesTypesAsParameters() {
        val container = db.findUnique(DateContainer::class.java, "VALUES (cast('2012-10-09 11:29:25' AS TIMESTAMP), cast('2012-10-09' AS DATE), cast('11:29:25' AS TIME))")

        assertThat(container.dateTime, `is`(LocalDateTime.of(2012, 10, 9, 11, 29, 25)))
        assertThat(container.date, `is`(LocalDate.of(2012, 10, 9)))
        assertThat(container.time, `is`(LocalTime.of(11, 29, 25)))
    }

    @Test
    fun saveJavaTimeTypes() {
        db.update("DROP TABLE IF EXISTS date_test")
        db.update("CREATE TABLE date_test (timestamp TIMESTAMP, date DATE, time TIME)")

        val dateTime = LocalDateTime.now()
        val date = LocalDate.now()
        val time = LocalTime.now().truncatedTo(SECONDS)

        db.update("INSERT INTO date_test (timestamp, date, time) VALUES (?, ?, ?)", dateTime, date, time)

        assertThat(db.findUnique(LocalDateTime::class.java, "SELECT timestamp FROM date_test"), `is`(dateTime))
        assertThat(db.findUnique(LocalDate::class.java, "SELECT date FROM date_test"), `is`(date))
        assertThat(db.findUnique(LocalTime::class.java, "SELECT time FROM date_test"), `is`(time))
    }

    @Test
    fun timeZoneConversions() {
        db.update("DROP TABLE IF EXISTS timezones")
        db.update("CREATE TEMPORARY TABLE timezones (zone_id VARCHAR(64))")

        val helsinkiTimeZone = ZoneId.of("Europe/Helsinki")

        db.update("INSERT INTO timezones (zone_id) VALUES (?)", helsinkiTimeZone)

        assertThat(db.findUnique(ZoneId::class.java, "SELECT zone_id FROM timezones"), `is`(helsinkiTimeZone))
    }

    class DateContainer
    @Reflective
    constructor(internal val dateTime: LocalDateTime, internal val date: LocalDate, internal val time: LocalTime)
}
