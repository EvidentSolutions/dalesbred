package org.dalesbred

import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import org.dalesbred.testutils.withSystemProperty
import java.util.*
import javax.naming.Context
import javax.naming.Name
import javax.naming.spi.InitialContextFactory
import javax.sql.DataSource
import kotlin.reflect.jvm.jvmName
import kotlin.test.Test
import kotlin.test.assertEquals

@DatabaseTest(POSTGRESQL)
class DatabaseJndiLookupTest {

    @Test
    fun `create Database by fetching DataSource from JNDI`(dataSource: DataSource) {
        withSystemProperty("java.naming.factory.initial", MyInitialFactory::class.jvmName) {
            MyInitialFactory.initialContext.bind("java:comp/env/foo", dataSource)

            val db = Database.forJndiDataSource("java:comp/env/foo")
            assertEquals(42, db.findUniqueInt("values (42)"))
        }
    }

    class MyInitialFactory : InitialContextFactory {

        override fun getInitialContext(environment: Hashtable<*, *>): Context = initialContext

        companion object {
            val initialContext: Context = MemoryContext()
        }
    }

    private class MemoryContext : Context {

        private val map = mutableMapOf<String, Any>()

        override fun lookup(name: Name): Any? = lookup(name.toString())

        override fun lookup(name: String): Any? = map[name]

        override fun bind(name: Name, obj: Any) {
            bind(name.toString(), obj)
        }

        override fun bind(name: String, obj: Any) {
            map.put(name, obj)
        }

        override fun rebind(name: Name, obj: Any) {
            rebind(name.toString(), obj)
        }

        override fun rebind(name: String, obj: Any) {
            map.put(name, obj)
        }

        override fun unbind(name: Name) {
            unbind(name.toString())
        }

        override fun unbind(name: String) {
            map.remove(name)
        }

        override fun rename(oldName: Name, newName: Name) {
            rename(oldName.toString(), newName.toString())
        }

        override fun close() {}

        override fun rename(oldName: String, newName: String) = throw UnsupportedOperationException()

        override fun list(name: Name) = throw UnsupportedOperationException()

        override fun list(name: String) = throw UnsupportedOperationException()

        override fun listBindings(name: Name) = throw UnsupportedOperationException()

        override fun listBindings(name: String) = throw UnsupportedOperationException()

        override fun destroySubcontext(name: Name) = throw UnsupportedOperationException()

        override fun destroySubcontext(name: String) = throw UnsupportedOperationException()

        override fun createSubcontext(name: Name) = throw UnsupportedOperationException()

        override fun createSubcontext(name: String) = throw UnsupportedOperationException()

        override fun lookupLink(name: Name) = throw UnsupportedOperationException()

        override fun lookupLink(name: String) = throw UnsupportedOperationException()

        override fun getNameParser(name: Name) = throw UnsupportedOperationException()

        override fun getNameParser(name: String) = throw UnsupportedOperationException()

        override fun composeName(name: Name, prefix: Name) = throw UnsupportedOperationException()

        override fun composeName(name: String, prefix: String) = throw UnsupportedOperationException()

        override fun addToEnvironment(propName: String, propVal: Any) = throw UnsupportedOperationException()

        override fun removeFromEnvironment(propName: String) = throw UnsupportedOperationException()

        override fun getEnvironment() = throw UnsupportedOperationException()

        override fun getNameInNamespace() = throw UnsupportedOperationException()
    }
}
