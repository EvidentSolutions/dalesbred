package org.dalesbred.query

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VariableResolversTest {

    @Test
    fun `test provider for map`() {
        val foo = Any()
        val bar = Any()
        val baz = Any()

        val variableResolver = VariableResolver.forMap(mapOf(
            "foo" to foo,
            "bar" to bar,
            "baz" to baz))

        assertEquals(foo, variableResolver.getValue("foo"))
        assertEquals(bar, variableResolver.getValue("bar"))
        assertEquals(baz, variableResolver.getValue("baz"))
    }

    @Test
    fun `test provider for bean`() {
        val bean = TestBean()
        val variableResolver = VariableResolver.forBean(bean)

        assertEquals(bean.foo, variableResolver.getValue("foo"))
        assertEquals(bean.isBar, variableResolver.getValue("bar"))
        assertEquals(bean.baz, variableResolver.getValue("baz"))
    }

    @Test
    fun `resolving unknown variable throws exception`() {
        val variableResolver = VariableResolver.forBean(TestBean())

        assertFailsWith<VariableResolutionException> {
            variableResolver.getValue("unknown")
        }
    }

    @Test
    fun `resolving private variable throws exception`() {
        val variableResolver = VariableResolver.forBean(TestBean())

        assertFailsWith<VariableResolutionException> {
            variableResolver.getValue("privateVariable")
        }
    }

    @Test
    fun `variable throwing is wrapped in variable resolution exception`() {
        val variableResolver = VariableResolver.forBean(TestBean())

        assertFailsWith<VariableResolutionException> {
            variableResolver.getValue("throwingVariable")
        }
    }

    @Test
    fun `map resolver for unknown map key`() {
        val variableResolver = VariableResolver.forMap(emptyMap<String, Any>())

        assertFailsWith<VariableResolutionException> {
            variableResolver.getValue("unknown")
        }
    }

    @Suppress("unused")
    private class TestBean {
        var foo = Any()
        val isBar = true
        val baz = "qwerty"
        private val privateVariable = "foo"

        val throwingVariable: String
            get() = throw RuntimeException()
    }
}
