package org.dalesbred

import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseCreationTest {

    @Test
    fun forUrlAndCredentials() {
        val db = Database.forUrlAndCredentials("jdbc:hsqldb:.", "sa", "")

        assertEquals(42, db.findUniqueInt("values (42)"))
    }
}
