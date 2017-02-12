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

import org.joda.time.DateTime
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DatabaseReflectiveInstantiationTest {

    private val db = TestDatabaseProvider.createInMemoryHSQLDatabase()

    @get:Rule val rule = TransactionalTestsRule(db)

    @Test
    fun fetchSimpleFieldsAndProperties() {
        val result = db.findUnique(MyResult::class.java, "select 42, 43 as field, 44 as property from (values (1))")

        assertEquals(42, result.constructor)
        assertEquals(43, result.field)
        assertEquals(44, result.property)
    }

    @Test
    fun fieldsAndPropertiesWithoutMatchingCase() {
        val result = db.findUnique(MyResult::class.java, "select 42, 43 as fiELD, 44 as proPERty from (values (1))")

        assertEquals(42, result.constructor)
        assertEquals(43, result.field)
        assertEquals(44, result.property)
    }

    @Test
    fun constructorBindingWithNullValuesAndConversions() {
        val result = db.findUnique(ConstructorNeedingConversion::class.java, "values (cast(null as datetime))")
        assertNull(result.dateTime)
    }

    @Test
    fun fieldBindingNullValuesAndConversions() {
        val result = db.findUnique(FieldNeedingConversion::class.java, "select (cast(null as datetime)) as date_time from (values (1))")
        assertNull(result.dateTime)
    }

    @Test
    fun setterBindingNullValuesAndConversions() {
        val result = db.findUnique(SetterNeedingConversion::class.java, "select (cast(null as datetime)) as date_time from (values (1))")
        assertNull(result.dateTime)
    }

    @Test
    fun fieldsWithUnderscores() {
        val result = db.findUnique(ClassWithUnderscoreFields::class.java, "select 'Fred' as first_name from (values (1))")
        assertEquals("Fred", result.first_name)
    }

    @Test
    fun settersWithUnderscores() {
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
