package org.dalesbred.testutils

import org.dalesbred.Database
import org.dalesbred.connection.ConnectionProvider
import org.dalesbred.connection.DataSourceConnectionProvider
import org.dalesbred.testutils.DatabaseProvider.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.PostgreSQLContainer
import java.io.PrintWriter
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLFeatureNotSupportedException
import javax.sql.DataSource

@ExtendWith(DatabaseContextResolver::class)
annotation class DatabaseTest(val provider: DatabaseProvider)

enum class DatabaseProvider {
    POSTGRESQL,
    MARIADB,
    MYSQL,
    HSQL,
}

class DatabaseContextResolver : ParameterResolver {

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        val type = parameterContext.parameter.type
        return type == DataSource::class.java
            || type == Database::class.java
            || type == ConnectionProvider::class.java
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        val type = parameterContext.parameter.type
        val databaseTest = extensionContext.requiredTestClass.getAnnotation(DatabaseTest::class.java)
            ?: error("Test class is not annotated with @DatabaseTest")
        val provider = databaseTest.provider

        val dataSource = TestDatabaseProvider.createDataSource(provider)
        return when (type) {
            DataSource::class.java -> dataSource
            ConnectionProvider::class.java -> DataSourceConnectionProvider(dataSource)
            Database::class.java -> Database(dataSource)
            else -> error("unsupported type: $type")
        }
    }
}

private object TestDatabaseProvider {

    private val postgresqlContainer: PostgreSQLContainer<*> by lazy {
        val container = PostgreSQLContainer("postgres:17")
        container.start()
        container
    }

    private val mariadbContainer: MariaDBContainer<*> by lazy {
        val container = MariaDBContainer("mariadb:10.5.5")
        container.start()
        container
    }

    private val myqlContainer: MySQLContainer<*> by lazy {
        val container = MySQLContainer("mysql:5.7.34")
        container.start()
        container
    }

    fun createDataSource(provider: DatabaseProvider): DataSource = when (provider) {
        POSTGRESQL -> dataSourceFor(postgresqlContainer)
        MYSQL -> dataSourceFor(myqlContainer)
        MARIADB -> dataSourceFor(mariadbContainer)
        HSQL -> DriverManagerDataSource("jdbc:hsqldb:mem:test;hsqldb.tx=mvcc", "sa", "")
    }

    private fun dataSourceFor(container: JdbcDatabaseContainer<*>): DataSource =
        DriverManagerDataSource(container.jdbcUrl, container.username, container.password)

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
