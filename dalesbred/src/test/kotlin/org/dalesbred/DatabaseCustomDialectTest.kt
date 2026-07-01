package org.dalesbred

import org.dalesbred.connection.ConnectionProvider
import org.dalesbred.dialect.DefaultDialect
import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import org.dalesbred.testutils.transactionalTest
import kotlin.test.Test
import kotlin.test.assertEquals

@DatabaseTest(POSTGRESQL)
class DatabaseCustomDialectTest(connectionProvider: ConnectionProvider) {

    private val db = Database(connectionProvider, UppercaseDialect)

    @Test
    fun `custom Dialect`() = transactionalTest(db) {
        db.update("drop table if exists my_table")
        db.update("create temporary table my_table (text varchar(64))")

        db.update("insert into my_table values (?)", "foo")

        assertEquals("FOO", db.findUnique(String::class.java, "select text from my_table"))
    }

    private object UppercaseDialect : DefaultDialect() {
        override fun valueToDatabase(value: Any) = value.toString().uppercase()
    }
}
