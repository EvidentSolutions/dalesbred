@file:Suppress("SqlResolve")

package org.dalesbred.dialect

import org.dalesbred.Database
import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import org.dalesbred.testutils.transactionalTest
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@DatabaseTest(POSTGRESQL)
class PostgreSQLDialectTest(private val db: Database) {

    @Test
    fun `enums as primitives`() = transactionalTest(db) {
        db.update("drop type if exists mood cascade")
        db.update("create type mood as enum ('SAD', 'HAPPY')")

        db.findUnique(Mood::class.java, "select 'SAD'::mood").javaClass
        assertEquals(Mood.SAD, db.findUnique(Mood::class.java, "select 'SAD'::mood"))
        assertNull(db.findUnique(Mood::class.java, "select null::mood"))
    }

    @Test
    fun `enums as constructor parameters`() = transactionalTest(db) {
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
    fun `timestamps as dates`() = transactionalTest(db) {
        val date = Date()

        assertEquals(date.time, db.findUnique(Date::class.java, "select ?::timestamp", date).time)
    }

    enum class Mood {
        SAD,
        HAPPY
    }

    class Movie(val name: String, val mood: Mood)
}
