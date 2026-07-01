package org.dalesbred.internal.instantiation

import org.dalesbred.annotation.DalesbredIgnore
import java.util.*
import kotlin.test.*

class PropertyAccessorTest {

    @Test
    fun `finding setters`() {
        assertNotEquals(Optional.empty(), PropertyAccessor.findAccessor(DepartmentWithSetters::class.java, "department_name"))
    }

    @Test
    fun `finding fields`() {
        assertNotEquals(Optional.empty(), PropertyAccessor.findAccessor(DepartmentWithFields::class.java, "department_name"))
    }

    @Test
    fun `ignored setters`() {
        assertEquals(Optional.empty(), PropertyAccessor.findAccessor(IgnoredValues::class.java, "ignoredMethod"))
    }

    @Test
    fun `ignored fields`() {
        assertEquals(Optional.empty(), PropertyAccessor.findAccessor(IgnoredValues::class.java, "ignoredField"))
    }

    @Test
    fun `nested paths with intermediate fields`() {
        val accessor = PropertyAccessor.findAccessor(NestedPaths::class.java, "namedField.name").orElse(null)

        assertNotNull(accessor)

        val paths = NestedPaths()
        accessor.set(paths, "foo")

        assertEquals("foo", paths.namedGetter.name)
    }

    @Test
    fun `nested paths with intermediate getters`() {
        val accessor = PropertyAccessor.findAccessor(NestedPaths::class.java, "namedGetter.name").orElse(null)

        assertNotNull(accessor)

        val paths = NestedPaths()
        accessor.set(paths, "foo")

        assertEquals("foo", paths.namedGetter.name)
    }

    @Test
    fun `nested paths with null fields`() {
        val accessor = PropertyAccessor.findAccessor(NestedPaths::class.java, "nullField.name").orElse(null)

        assertNotNull(accessor)

        assertFailsWith<InstantiationFailureException> {
            accessor.set(NestedPaths(), "foo")
        }
    }

    @Test
    fun `nested paths with null getters`() {
        val accessor = PropertyAccessor.findAccessor(NestedPaths::class.java, "nullGetter.name").orElse(null)

        assertNotNull(accessor)

        assertFailsWith<InstantiationFailureException> {
            accessor.set(NestedPaths(), "foo")
        }
    }

    @Test
    fun `invalid path elements`() {
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
