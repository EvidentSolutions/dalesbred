package org.dalesbred

import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import org.dalesbred.testutils.transactionalTest
import kotlin.test.Test
import kotlin.test.assertEquals

@DatabaseTest(POSTGRESQL)
class DatabasePropertyPathBindingTest(private val db: Database) {

    @Test
    fun `binding to nested paths`() = transactionalTest(db) {
        val result = db.findUnique(ResultClass::class.java, """
                select 'AAA' as "nestedField.foo",
                       'BBB' as "nestedGetter.foo"
                    from (VALUES (0))""")

        assertEquals("AAA", result.nestedField.foo)
        assertEquals("BBB", result.nestedGetter.foo)
    }

    class ResultClass {
        val nestedField = NestedClass()
        val nestedGetter = NestedClass()
    }

    class NestedClass {
        lateinit var foo: String
    }
}
