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

package org.dalesbred.integration.java8

import org.dalesbred.TestDatabaseProvider
import org.dalesbred.testutils.transactionalTest
import org.dalesbred.testutils.withUTCTimeZone
import java.time.*
import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoUnit.SECONDS
import kotlin.test.Test
import kotlin.test.assertEquals

class JavaTimeIntegrationTest {

    private val db = TestDatabaseProvider.createPostgreSQLDatabase()

    @Test
    fun fetchLocalDateTime() = transactionalTest(db) {
        assertEquals(LocalDateTime.of(2012, 10, 9, 11, 29, 25), db.findUnique(LocalDateTime::class.java, "VALUES (cast('2012-10-09 11:29:25' AS TIMESTAMP))"))
    }

    @Test
    fun fetchInstant() = transactionalTest(db) {
        withUTCTimeZone {
            val time = Instant.ofEpochMilli(1295000000000L)
            assertEquals(time, db.findUnique(Instant::class.java, "VALUES (cast('2011-01-14 10:13:20' AS TIMESTAMP))"))
        }
    }

    @Test
    fun storeInstant() = transactionalTest(db) {
        db.update("DROP TABLE IF EXISTS instant_test")
        db.update("CREATE TABLE instant_test (timestamp TIMESTAMP)")

        val instant = Instant.now().truncatedTo(ChronoUnit.MILLIS)

        db.update("INSERT INTO instant_test (timestamp) VALUES (?)", instant)

        assertEquals(instant, db.findUnique(Instant::class.java, "SELECT timestamp FROM instant_test"))
    }

    @Test
    fun fetchLocalDates() = transactionalTest(db) {
        assertEquals(LocalDate.of(2012, 10, 9), db.findUnique(LocalDate::class.java, "VALUES (cast('2012-10-09' AS DATE))"))
    }

    @Test
    fun fetchLocalTime() = transactionalTest(db) {
        assertEquals(LocalTime.of(11, 29, 25), db.findUnique(LocalTime::class.java, "VALUES (cast('11:29:25' AS TIME))"))
    }

    @Test
    fun localDatesWithTimeZoneProblems() = transactionalTest(db) {
        withUTCTimeZone {
            assertEquals(LocalDate.of(2012, 10, 9), db.findUnique(LocalDate::class.java, "VALUES (cast('2012-10-09' AS DATE))"))
        }
    }

    @Test
    fun localDatesFromTimestampWithTimeZoneProblems() = transactionalTest(db) {
        withUTCTimeZone {
            assertEquals(LocalDate.of(2012, 10, 9), db.findUnique(LocalDate::class.java, "VALUES (cast('2012-10-09 00:00:00' AS TIMESTAMP))"))
        }
    }

    @Test
    fun timesTypesAsParameters() = transactionalTest(db) {
        val container = db.findUnique(DateContainer::class.java, "VALUES (cast('2012-10-09 11:29:25' AS TIMESTAMP), cast('2012-10-09' AS DATE), cast('11:29:25' AS TIME))")

        assertEquals(LocalDateTime.of(2012, 10, 9, 11, 29, 25), container.dateTime)
        assertEquals(LocalDate.of(2012, 10, 9), container.date)
        assertEquals(LocalTime.of(11, 29, 25), container.time)
    }

    @Test
    fun saveJavaTimeTypes() = transactionalTest(db) {
        db.update("DROP TABLE IF EXISTS date_test")
        db.update("CREATE TABLE date_test (timestamp TIMESTAMP, date DATE, time TIME)")

        val dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val date = LocalDate.now()
        val time = LocalTime.now().truncatedTo(SECONDS)

        db.update("INSERT INTO date_test (timestamp, date, time) VALUES (?, ?, ?)", dateTime, date, time)

        assertEquals(dateTime, db.findUnique(LocalDateTime::class.java, "SELECT timestamp FROM date_test"))
        assertEquals(date, db.findUnique(LocalDate::class.java, "SELECT date FROM date_test"))
        assertEquals(time, db.findUnique(LocalTime::class.java, "SELECT time FROM date_test"))
    }

    @Test
    fun timeZoneConversions() = transactionalTest(db) {
        db.update("DROP TABLE IF EXISTS timezones")
        db.update("CREATE TEMPORARY TABLE timezones (zone_id VARCHAR(64))")

        val helsinkiTimeZone = ZoneId.of("Europe/Helsinki")

        db.update("INSERT INTO timezones (zone_id) VALUES (?)", helsinkiTimeZone)

        assertEquals(helsinkiTimeZone, db.findUnique(ZoneId::class.java, "SELECT zone_id FROM timezones"))
    }

    class DateContainer(val dateTime: LocalDateTime, val date: LocalDate, val time: LocalTime)
}
