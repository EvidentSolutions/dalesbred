package org.dalesbred

import org.dalesbred.query.SqlQuery
import org.dalesbred.result.NonUniqueResultException
import org.dalesbred.result.UnexpectedResultException
import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import org.dalesbred.testutils.transactionalTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

@DatabaseTest(POSTGRESQL)
class DatabaseExceptionsTest(private val db: Database) {

    @Test
    fun `findUniqueOrNull non-unique result`() = transactionalTest(db) {
        val query = SqlQuery.query("values (1), (2)")
        val error = assertFailsWith<NonUniqueResultException> {
            db.findUniqueOrNull(Int::class.java, query)
        }
        assertSame(query, error.query)
    }

    @Test
    fun `findOptional non-unique result`() = transactionalTest(db) {
        val query = SqlQuery.query("values (1), (2)")
        val error = assertFailsWith<NonUniqueResultException> {
            db.findOptional(Int::class.java, query)
        }
        assertSame(query, error.query)
    }

    @Test
    fun `findUniqueInt null result`() = transactionalTest(db) {
        val query = SqlQuery.query("values (cast(null as int))")

        val error = assertFailsWith<UnexpectedResultException> {
            db.findUniqueInt(query)
        }
        assertSame(query, error.query)
    }
}
