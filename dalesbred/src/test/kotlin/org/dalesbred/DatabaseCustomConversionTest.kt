package org.dalesbred

import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import org.dalesbred.testutils.transactionalTest
import kotlin.test.Test
import kotlin.test.assertEquals

@DatabaseTest(POSTGRESQL)
class DatabaseCustomConversionTest(private val db: Database) {

    @Test
    fun `custom load conversions`() = transactionalTest(db) {
        db.typeConversionRegistry.registerConversionFromDatabase(String::class.java, EmailAddress::class.java) { EmailAddress.parse(it) }

        assertEquals(EmailAddress("user", "example.org"), db.findUnique(EmailAddress::class.java, "values ('user@example.org')"))
    }

    @Test
    fun `custom save conversions`() = transactionalTest(db) {
        db.typeConversionRegistry.registerConversionToDatabase(EmailAddress::class.java) { it.toString() }

        db.update("drop table if exists custom_save_conversions_test")
        db.update("create temporary table custom_save_conversions_test (email varchar(32))")

        db.update("insert into custom_save_conversions_test (email) values (?)", EmailAddress("user", "example.org"))

        assertEquals("user@example.org", db.findUnique(String::class.java, "select email from custom_save_conversions_test"))
    }

    data class EmailAddress(private val user: String, private val host: String) {

        override fun toString() = "$user@$host"

        companion object {

            private val AT_SIGN = Regex("@")

            fun parse(value: String): EmailAddress {
                val parts = AT_SIGN.split(value)
                if (parts.size == 2)
                    return EmailAddress(parts[0], parts[1])
                throw IllegalArgumentException("invalid address: '$value'")
            }
        }
    }
}
