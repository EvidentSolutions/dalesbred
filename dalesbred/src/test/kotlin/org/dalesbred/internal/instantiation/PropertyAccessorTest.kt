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

package org.dalesbred.internal.instantiation

import org.dalesbred.annotation.DalesbredIgnore
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class PropertyAccessorTest {

    @Test
    fun findingSetters() {
        assertNotEquals(Optional.empty(), PropertyAccessor.findAccessor(DepartmentWithSetters::class.java, "department_name"))
    }

    @Test
    fun findingFields() {
        assertNotEquals(Optional.empty(), PropertyAccessor.findAccessor(DepartmentWithFields::class.java, "department_name"))
    }

    @Test
    fun ignoredSetters() {
        assertEquals(Optional.empty(), PropertyAccessor.findAccessor(IgnoredValues::class.java, "ignoredMethod"))
    }

    @Test
    fun ignoredFields() {
        assertEquals(Optional.empty(), PropertyAccessor.findAccessor(IgnoredValues::class.java, "ignoredField"))
    }

    @Test
    fun nestedPathsWithIntermediateFields() {
        val accessor = PropertyAccessor.findAccessor(NestedPaths::class.java, "namedField.name").orElse(null)

        assertNotNull(accessor)

        val paths = NestedPaths()
        accessor.set(paths, "foo")

        assertEquals("foo", paths.namedGetter.name)
    }

    @Test
    fun nestedPathsWithIntermediateGetters() {
        val accessor = PropertyAccessor.findAccessor(NestedPaths::class.java, "namedGetter.name").orElse(null)

        assertNotNull(accessor)

        val paths = NestedPaths()
        accessor.set(paths, "foo")

        assertEquals("foo", paths.namedGetter.name)
    }

    @Test
    fun nestedPathsWithNullFields() {
        val accessor = PropertyAccessor.findAccessor(NestedPaths::class.java, "nullField.name").orElse(null)

        assertNotNull(accessor)

        assertFailsWith<InstantiationFailureException> {
            accessor.set(NestedPaths(), "foo")
        }
    }

    @Test
    fun nestedPathsWithNullGetters() {
        val accessor = PropertyAccessor.findAccessor(NestedPaths::class.java, "nullGetter.name").orElse(null)

        assertNotNull(accessor)

        assertFailsWith<InstantiationFailureException> {
            accessor.set(NestedPaths(), "foo")
        }
    }

    @Test
    fun invalidPathElements() {
        assertEquals(Optional.empty(), PropertyAccessor.findAccessor(Named::class.java, "foo.name"))
    }

    @Suppress("unused")
    private class DepartmentWithFields {
        @JvmField var departmentName: String? = null
    }

    @Suppress("unused")
    class DepartmentWithSetters {
        var departmentName: String? = null
    }

    @Suppress("unused")
    class NestedPaths {

        @JvmField val namedField = Named()

        @JvmField val nullField: Named? = null

        val namedGetter: Named
            get() = namedField

        val nullGetter: Named?
            get() = null
    }

    @Suppress("unused")
    class Named {
        @JvmField var name: String? = null
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    class IgnoredValues {

        @DalesbredIgnore
        @JvmField var ignoredField: String? = null

        @DalesbredIgnore
        fun setIgnoredMethod(s: String) {
        }
    }
}
