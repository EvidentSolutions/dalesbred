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

package org.dalesbred

import org.dalesbred.connection.ConnectionProvider
import org.dalesbred.connection.DriverManagerConnectionProvider
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.io.PrintWriter
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLFeatureNotSupportedException
import javax.sql.DataSource

object TestDatabaseProvider {

    private val postgresqlContainer: PostgreSQLContainer<*> by lazy {
        val container = PostgreSQLContainer(DockerImageName.parse("postgres:17"))
        container.start()
        container
    }

    private val mariadbContainer: MariaDBContainer<*> by lazy {
        val container = MariaDBContainer(DockerImageName.parse("mariadb:10.5.5"))
        container.start()
        container
    }

    private val myqlContainer: MySQLContainer<*> by lazy {
        val container = MySQLContainer(DockerImageName.parse("mysql:5.7.34"))
        container.start()
        container
    }

    fun createInMemoryHSQLDatabase() =
        Database.forUrlAndCredentials("jdbc:hsqldb:mem:test;hsqldb.tx=mvcc", "sa", "")

    fun createPostgreSQLDatabase(): Database =
        Database(connectionProviderFor(postgresqlContainer))

    private fun connectionProviderFor(container: JdbcDatabaseContainer<*>) =
        DriverManagerConnectionProvider(container.jdbcUrl, container.username, container.password)

    fun createMySQLConnectionProvider() =
        connectionProviderFor(myqlContainer)

    fun createMariaDBConnectionProvider() =
        connectionProviderFor(mariadbContainer)

    fun createInMemoryHSQLConnectionProvider(): ConnectionProvider =
        DriverManagerConnectionProvider("jdbc:hsqldb:.", "sa", "")

    fun createInMemoryHSQLDataSource(): DataSource =
        DriverManagerDataSource("jdbc:hsqldb:.", "sa", "")

    private class DriverManagerDataSource(
        private val url: String,
        private val defaultUser: String?,
        private val defaultPassword: String?
    ) : DataSource {

        override fun getConnection(): Connection {
            return getConnection(defaultUser, defaultPassword)
        }

        override fun getConnection(username: String?, password: String?): Connection {
            return DriverManager.getConnection(url, username, password)
        }

        override fun getLogWriter() = throw SQLFeatureNotSupportedException()

        override fun setLogWriter(out: PrintWriter) = throw SQLFeatureNotSupportedException()

        override fun setLoginTimeout(seconds: Int) = throw SQLFeatureNotSupportedException()

        override fun getLoginTimeout() = throw SQLFeatureNotSupportedException()

        override fun getParentLogger() = throw SQLFeatureNotSupportedException()

        override fun <T> unwrap(iface: Class<T>) = throw SQLFeatureNotSupportedException()

        override fun isWrapperFor(iface: Class<*>) = throw SQLFeatureNotSupportedException()
    }
}
