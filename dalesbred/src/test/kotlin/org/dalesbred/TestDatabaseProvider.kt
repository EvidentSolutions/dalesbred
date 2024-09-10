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
import org.dalesbred.transaction.TransactionCallback
import org.junit.Assume.assumeFalse
import java.io.InputStream
import java.io.PrintWriter
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLFeatureNotSupportedException
import java.util.*
import javax.sql.DataSource

object TestDatabaseProvider {

    fun createInMemoryHSQLDatabase() =
        Database.forUrlAndCredentials("jdbc:hsqldb:mem:test;hsqldb.tx=mvcc", "sa", "")

    fun createPostgreSQLDatabase(): Database {
        val host = System.getenv("POSTGRES_HOST")
        if (host != null) {
            val port = System.getenv("POSTGRES_PORT")?.toInt() ?: 5432
            val user = System.getenv("POSTGRES_USER") ?: "postgres"
            val password = System.getenv("POSTGRES_PASSWORD") ?: "password"
            val database = System.getenv("POSTGRES_DATABASE") ?: user
            val url = "jdbc:postgresql://$host:$port/$database"

            return Database(DriverManagerConnectionProvider(url, user, password))
        }

        return Database(createConnectionProviderFromProperties("postgresql-connection.properties"))
    }

    fun createMySQLConnectionProvider() =
        createConnectionProviderFromProperties("mysql-connection.properties")

    fun createMariaDBConnectionProvider() =
        createConnectionProviderFromProperties("mariadb-connection.properties")

    fun createInMemoryHSQLConnectionProvider(): ConnectionProvider =
        DriverManagerConnectionProvider("jdbc:hsqldb:.", "sa", "")

    fun createInMemoryHSQLDataSource(): DataSource =
        DriverManagerDataSource("jdbc:hsqldb:.", "sa", "")

    private fun createConnectionProviderFromProperties(propertiesFile: String): ConnectionProvider {
        val props = loadProperties(propertiesFile)
        val url = props.getProperty("jdbc.url")
        val login = props.getProperty("jdbc.login")
        val password = props.getProperty("jdbc.password")

        return DriverManagerConnectionProvider(url, login, password)
    }

    private fun loadProperties(name: String): Properties {
        val stream: InputStream? = TransactionCallback::class.java.classLoader.getResourceAsStream(name)
        assumeFalse("ignored test because '$name' was not found", stream == null)
        stream!!.use {
            return Properties().apply {
                load(stream)
            }
        }
    }

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
