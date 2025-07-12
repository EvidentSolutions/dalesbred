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

package org.dalesbred.integration.spring

import org.dalesbred.Database
import org.dalesbred.TestDatabaseProvider
import org.dalesbred.transaction.Propagation
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.transaction.support.TransactionTemplate
import javax.sql.DataSource
import kotlin.test.Test
import kotlin.test.assertEquals

class SpringConfigurationTest {

    @Test
    fun dalesbredUsesConnectionBoundToSpringTransactions() {
        val ctx = AnnotationConfigApplicationContext(SimpleConfiguration::class.java)
        val dataSource = ctx.getBean(DataSource::class.java)
        val db = ctx.getBean(Database::class.java)

        TransactionTemplate(DataSourceTransactionManager(dataSource)).execute {
            db.withTransaction(Propagation.MANDATORY) { tx ->
                assertEquals(DataSourceUtils.getConnection(dataSource), tx.connection)
            }
        }
    }

    @Test
    fun rollbackForSpringTransactionDiscardsChangesOfDalesbred() {
        val ctx = AnnotationConfigApplicationContext(SimpleConfiguration::class.java)
        val dataSource = ctx.getBean(DataSource::class.java)
        val db = ctx.getBean(Database::class.java)

        db.update("drop table if exists spring_tx_test")
        db.update("create table spring_tx_test (id int)")

        TransactionTemplate(DataSourceTransactionManager(dataSource)).execute { status ->
            db.update("insert into spring_tx_test (id) values (1)")
            status.setRollbackOnly()
        }

        assertEquals(0, db.findUniqueInt("select count(*) from spring_tx_test"))
    }

    @Configuration
    open class SimpleConfiguration : DalesbredConfigurationSupport() {

        @Bean
        open fun dataSource() = TestDatabaseProvider.createInMemoryHSQLDataSource()

        @Bean
        open fun transactionManager() = DataSourceTransactionManager(dataSource())
    }
}
