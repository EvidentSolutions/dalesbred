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
import java.sql.Connection
import java.sql.DriverManager
import javax.sql.DataSource

@ExtendWith(DatabaseResolver::class)
annotation class DatabaseTest(val provider: DatabaseProvider)

enum class DatabaseProvider {
    POSTGRESQL,
    MARIADB,
    MYSQL,
    HSQL,
}

private class DatabaseResolver : ParameterResolver {

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

    private val postgresqlContainer by lazy {
        PostgreSQLContainer("postgres:17").also { it.start() }
    }

    private val mariadbContainer by lazy {
        MariaDBContainer("mariadb:10.5.5").also { it.start() }
    }

    private val myqlContainer by lazy {
        MySQLContainer("mysql:5.7.34").also { it.start() }
    }

    fun createDataSource(provider: DatabaseProvider): DataSource = when (provider) {
        POSTGRESQL -> dataSourceFor(postgresqlContainer)
        MYSQL -> dataSourceFor(myqlContainer)
        MARIADB -> dataSourceFor(mariadbContainer)
        HSQL -> DriverManagerDataSource("jdbc:hsqldb:mem:test", "sa", "")
    }

    private fun dataSourceFor(container: JdbcDatabaseContainer<*>): DataSource =
        DriverManagerDataSource(container.jdbcUrl, container.username, container.password)

    @Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
    private class DriverManagerDataSource(
        private val url: String,
        private val defaultUser: String,
        private val defaultPassword: String
    ) : DataSource by unimplemented() {

        override fun getConnection(): Connection =
            getConnection(defaultUser, defaultPassword)

        override fun getConnection(username: String?, password: String?): Connection =
            DriverManager.getConnection(url, username, password)
    }
}
