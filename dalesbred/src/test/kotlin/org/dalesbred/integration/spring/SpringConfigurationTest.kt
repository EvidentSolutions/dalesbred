@file:Suppress("SqlResolve")

package org.dalesbred.integration.spring

import org.dalesbred.Database
import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import org.dalesbred.transaction.Propagation
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.transaction.support.TransactionTemplate
import javax.sql.DataSource
import kotlin.test.Test
import kotlin.test.assertEquals

@DatabaseTest(POSTGRESQL)
class SpringConfigurationTest(private val dataSource: DataSource) {

    val tm = DataSourceTransactionManager(dataSource)
    val db = Database(SpringTransactionManager(dataSource, tm))

    @Test
    fun `Dalesbred uses connection bound to Spring transactions`() {
        TransactionTemplate(tm).execute {
            db.withTransaction(Propagation.MANDATORY) { tx ->
                assertEquals(DataSourceUtils.getConnection(dataSource), tx.connection)
            }
        }
    }

    @Test
    fun `rollback for Spring transaction discards changes of Dalesbred`() {
        db.update("drop table if exists spring_tx_test")
        db.update("create table spring_tx_test (id int)")

        TransactionTemplate(DataSourceTransactionManager(dataSource)).execute { status ->
            db.update("insert into spring_tx_test (id) values (1)")
            status.setRollbackOnly()
        }

        assertEquals(0, db.findUniqueInt("select count(*) from spring_tx_test"))
    }
}
