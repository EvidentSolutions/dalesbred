package org.dalesbred

import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import org.dalesbred.testutils.transactionalTest
import org.joda.time.DateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@DatabaseTest(POSTGRESQL)
class DatabaseReflectiveInstantiationTest(private val db: Database) {

    @Test
    fun `fetch simple fields and properties`() = transactionalTest(db) {
        val result = db.findUnique(MyResult::class.java, "select 42, 43 as field, 44 as property from (values (1))")

        assertEquals(42, result.constructor)
        assertEquals(43, result.field)
        assertEquals(44, result.property)
    }

    @Test
    fun `fields and properties without matching case`() = transactionalTest(db) {
        val result = db.findUnique(MyResult::class.java, "select 42, 43 as fiELD, 44 as proPERty from (values (1))")

        assertEquals(42, result.constructor)
        assertEquals(43, result.field)
        assertEquals(44, result.property)
    }

    @Test
    fun `constructor binding with null values and conversions`() = transactionalTest(db) {
        val result = db.findUnique(ConstructorNeedingConversion::class.java, "values (cast(null as timestamp))")
        assertNull(result.dateTime)
    }

    @Test
    fun `field binding null values and conversions`() = transactionalTest(db) {
        val result = db.findUnique(FieldNeedingConversion::class.java, "select (cast(null as timestamp)) as date_time from (values (1))")
        assertNull(result.dateTime)
    }

    @Test
    fun `setter binding null values and conversions`() = transactionalTest(db) {
        val result = db.findUnique(SetterNeedingConversion::class.java, "select (cast(null as timestamp)) as date_time from (values (1))")
        assertNull(result.dateTime)
    }

    @Test
    fun `fields with underscores`() = transactionalTest(db) {
        val result = db.findUnique(ClassWithUnderscoreFields::class.java, "select 'Fred' as first_name from (values (1))")
        assertEquals("Fred", result.first_name)
    }

    @Test
    fun `setters with underscores`() = transactionalTest(db) {
        val result = db.findUnique(ClassWithUnderscoreSetters::class.java, "select 'Fred' as first_name from (values (1))")
        assertEquals("Fred", result.first_name)
    }

    class MyResult(val constructor: Int) {

        var field: Int = 0
        var property: Int = 0
    }

    class ConstructorNeedingConversion(val dateTime: DateTime?)

    class FieldNeedingConversion {

        @JvmField
        var dateTime: DateTime? = null
    }

    class SetterNeedingConversion {
        var dateTime: DateTime? = null
    }

    class ClassWithUnderscoreFields {

        @JvmField
        var first_name: String? = null
    }

    class ClassWithUnderscoreSetters {

        lateinit var first_name: String
    }
}
