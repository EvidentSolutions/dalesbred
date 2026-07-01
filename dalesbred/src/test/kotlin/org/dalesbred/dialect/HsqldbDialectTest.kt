package org.dalesbred.dialect

import org.dalesbred.Database
import org.dalesbred.testutils.transactionalTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HsqldbDialectTest {

    private val db = Database.forUrlAndCredentials("jdbc:hsqldb:.", "sa", "")

    @Test
    fun `simple query`() = transactionalTest(db) {
        assertEquals(42, db.findUniqueInt("VALUES (42)"))
    }

    @Test
    fun `detect dialect`() = transactionalTest(db) {
        db.withVoidTransaction { tx ->
            val dialect = Dialect.detect(tx.connection)

            assertTrue { dialect is HsqldbDialect }
        }
    }

    @Test
    fun `enums as primitives`() = transactionalTest(db) {
        assertEquals(Mood.SAD, db.findUnique(Mood::class.java, "values ('SAD')"))
        assertNull(db.findUnique(Mood::class.java, "values (cast(null as varchar(20)))"))
    }

    @Test
    fun `enums as constructor parameters`() = transactionalTest(db) {
        db.update("drop table if exists movie")
        db.update("create temporary table movie (name varchar(64) primary key, mood varchar(20) not null)")

        db.update("insert into movie (name, mood) values (?, ?)", "Amélie", Mood.HAPPY)

        val movie = db.findUnique(Movie::class.java, "select name, mood from movie")
        assertEquals("Amélie", movie.name)
        assertEquals(Mood.HAPPY, movie.mood)
    }

    enum class Mood {
        SAD,
        HAPPY
    }

    class Movie(val name: String, val mood: Mood)
}
