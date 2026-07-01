package org.dalesbred

import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import javax.sql.DataSource
import kotlin.test.Test
import kotlin.test.assertEquals

@DatabaseTest(POSTGRESQL)
class DatabaseDataSourceTest {

    @Test
    fun `creating Database from DataSource`(dataSource: DataSource) {
        val db = Database.forDataSource(dataSource)

        assertEquals(42, db.findUniqueInt("values (42)"))
    }
}
