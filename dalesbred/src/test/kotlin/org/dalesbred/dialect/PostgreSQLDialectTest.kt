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

package org.dalesbred.dialect

import org.dalesbred.TestDatabaseProvider
import org.dalesbred.TransactionalTestsRule
import org.junit.Rule
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PostgreSQLDialectTest {

    private val db = TestDatabaseProvider.createPostgreSQLDatabase()

    @get:Rule
    val rule = TransactionalTestsRule(db)

    @Test
    fun enumsAsPrimitives() {
        db.update("drop type if exists mood cascade")
        db.update("create type mood as enum ('SAD', 'HAPPY')")

        db.findUnique(Mood::class.java, "select 'SAD'::mood").javaClass
        assertEquals(Mood.SAD, db.findUnique(Mood::class.java, "select 'SAD'::mood"))
        assertNull(db.findUnique(Mood::class.java, "select null::mood"))
    }

    @Test
    fun enumsAsConstructorParameters() {
        db.typeConversionRegistry.registerNativeEnumConversion(Mood::class.java, "mood")

        db.update("drop type if exists mood cascade")
        db.update("create type mood as enum ('SAD', 'HAPPY')")

        db.update("drop table if exists movie")
        db.update("create temporary table movie (name varchar(64) primary key, mood mood not null)")

        db.update("insert into movie (name, mood) values (?, ?)", "Amélie", Mood.HAPPY)

        val movie = db.findUnique(Movie::class.java, "select name, mood from movie")
        assertEquals("Amélie", movie.name)
        assertEquals(Mood.HAPPY, movie.mood)
    }

    @Test
    fun `bind enums as other`() {

        db.update("drop type if exists mood cascade")
        db.update("create type mood as enum ('SAD', 'HAPPY')")

        db.update("drop table if exists movie")
        db.update("create temporary table movie (name varchar(64) primary key, mood mood not null)")

        db.update("insert into movie (name, mood) values (?, ?)", "Amélie", Mood.HAPPY)

        val movie = db.findUnique(Movie::class.java, "select name, mood from movie")
        assertEquals("Amélie", movie.name)
        assertEquals(Mood.HAPPY, movie.mood)
    }

    @Test
    fun dates() {
        val date = Date()

        assertEquals(date.time, db.findUnique(Date::class.java, "select ?::timestamp", date).time)
    }

    enum class Mood {
        SAD,
        HAPPY
    }

    class Movie(val name: String, val mood: Mood)
}
