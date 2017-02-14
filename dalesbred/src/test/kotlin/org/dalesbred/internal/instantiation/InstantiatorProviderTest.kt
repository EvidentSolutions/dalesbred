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
import org.dalesbred.annotation.DalesbredInstantiator
import org.dalesbred.dialect.DefaultDialect
import org.dalesbred.internal.instantiation.test.InaccessibleClassRef
import org.dalesbred.internal.utils.TypeUtils
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InstantiatorProviderTest {

    private val instantiatorRegistry = InstantiatorProvider(DefaultDialect())

    @Test
    fun everyClassIsAssignableFromItself() {
        assertAssignable(Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!)
        assertAssignable(Int::class.java, Int::class.java)
        assertAssignable(Any::class.java, Any::class.java)
        assertAssignable(String::class.java, String::class.java)
    }

    @Test
    fun primitivesAreAssignableFromWrappers() {
        assertAssignable(Int::class.javaPrimitiveType!!, Int::class.java)
        assertAssignable(Long::class.javaPrimitiveType!!, Long::class.java)
    }

    @Test
    fun wrappersAreAssignableFromPrimitives() {
        assertAssignable(Int::class.java, Int::class.javaPrimitiveType!!)
        assertAssignable(Long::class.java, Long::class.javaPrimitiveType!!)
    }

    @Test
    fun findDefaultConstructor() {
        val result = assertNotNull(instantiate(TestClass::class.java, NamedTypeList.builder(0).build()))
        assertEquals(1, result.calledConstructor)
    }

    @Test
    fun findConstructedBasedOnType() {
        val result = assertNotNull(instantiate(TestClass::class.java, String::class.java, "foo"))
        assertEquals(2, result.calledConstructor)
    }

    @Test
    fun findBasedOnPrimitiveType() {
        val result = assertNotNull(instantiate(TestClass::class.java, Int::class.javaPrimitiveType!!, 3))
        assertEquals(3, result.calledConstructor)
    }

    @Test
    fun findPrimitiveTypedConstructorWithBoxedType() {
        val result = assertNotNull(instantiate(TestClass::class.java, Int::class.java, 3))
        assertEquals(3, result.calledConstructor)
    }

    @Test
    fun findingInstantiatorForInaccessibleClassThrowsNiceException() {
        assertFailsWith<InstantiationFailureException> {
            instantiate(InaccessibleClassRef.INACCESSIBLE_CLASS, Int::class.javaPrimitiveType!!, 3)
        }
    }

    @Test
    fun findingInstantiatorForInaccessibleConstructorThrowsNiceException() {
        assertFailsWith<InstantiationFailureException> {
            instantiate(InaccessibleConstructor::class.java, Int::class.javaPrimitiveType!!, 3)
        }
    }

    @Test
    fun extraFieldsCanBeSpecifiedWithSettersAndFields() {
        val types = NamedTypeList.builder(3).apply {
            add("arg", String::class.java)
            add("propertyWithAccessors", String::class.java)
            add("publicField", String::class.java)
        }.build()

        val result = assertNotNull(instantiate(TestClass::class.java, types, "foo", "bar", "baz"))
        assertEquals(2, result.calledConstructor)
        assertEquals("bar", result.propertyWithAccessors)
        assertEquals("baz", result.publicField)
    }

    @Test
    fun dontUseIgnoredConstructor() {
        assertFailsWith<InstantiationFailureException> {
            instantiate(TestClass::class.java, createNamedTypeList(Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!), 0, 0)
        }
    }

    @Test
    fun explicitConstructorIsUsedInsteadOfValidConstructor() {
        val types = NamedTypeList.builder(1)
                .add("publicField", String::class.java).build()

        assertFailsWith<InstantiationFailureException> {
            instantiate(TestClassWithExplicitConstructor::class.java, types, "foo")
        }
    }

    @Test
    fun explicitConstructorIsUsedInsteadOfValidPropertyAccessor() {
        val types = NamedTypeList.builder(1)
                .add("propertyWithAccessors", String::class.java).build()

        assertFailsWith<InstantiationFailureException> {
            instantiate(TestClassWithExplicitConstructor::class.java, types, "bar")
        }
    }

    @Test
    fun explicitPrivateConstructor() {
        val types = NamedTypeList.builder(1)
                .add("publicField", String::class.java).build()

        val result = assertNotNull(instantiate(TestClassWithExplicitPrivateConstructor::class.java, types, "foo"))

        assertEquals(2, result.calledConstructor)
        assertEquals("foo", result.publicField)
    }

    @Test
    fun multipleConstructorAnnotationsGivesNiceError() {
        assertFailsWith<InstantiationFailureException> {
            instantiate(TestClassWithMultipleExplicitConstructors::class.java, Int::class.javaPrimitiveType!!, 1)
        }
    }

    @Test
    fun privateClassesCanBeInstantiatedWithExplicitAnnotation() {
        val types = NamedTypeList.builder(1)
                .add("publicField", String::class.java).build()
        val result = assertNotNull(instantiate(PrivateTestClassWithExplicitInstantiator::class.java, types, "foo"))

        assertEquals("foo", result.publicField)
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    class TestClass {
        val calledConstructor: Int

        var publicField = ""

        var propertyWithAccessors = ""

        constructor() {
            calledConstructor = 1
        }

        constructor(s: String) {
            calledConstructor = 2
        }

        constructor(x: Int) {
            calledConstructor = 3
        }

        @DalesbredIgnore
        constructor(x: Int, y: Int) {
            calledConstructor = 4
        }
    }

    @Suppress("unused")
    class TestClassWithExplicitConstructor {
        var publicField = ""

        var propertyWithAccessors = ""

        constructor()

        constructor(publicField: String) {
            this.publicField = publicField
        }

        @DalesbredInstantiator
        constructor(wrongType: Int) {
            this.publicField = wrongType.toString()
        }
    }

    class TestClassWithExplicitPrivateConstructor {
        val calledConstructor: Int
        var publicField = ""

        @Suppress("unused")
        constructor() {
            calledConstructor = 1
        }

        @DalesbredInstantiator
        private constructor(publicField: String) {
            calledConstructor = 2
            this.publicField = publicField
        }
    }

    class TestClassWithMultipleExplicitConstructors {
        @DalesbredInstantiator constructor()
        @DalesbredInstantiator constructor(@Suppress("UNUSED_PARAMETER") foo: String)
    }

    private class PrivateTestClassWithExplicitInstantiator
    @DalesbredInstantiator private constructor(val publicField: String)

    private fun <T, V> instantiate(cl: Class<T>, type: Class<V>, value: V): T? {
        return instantiate(cl, createNamedTypeList(type), value)
    }

    private fun <T> instantiate(cl: Class<T>, namedTypeList: NamedTypeList, vararg values: Any?): T? {
        val instantiator = instantiatorRegistry.findInstantiator(cl, namedTypeList)
        @Suppress("UNCHECKED_CAST")
        val arguments = InstantiatorArguments(namedTypeList, values as Array<Any>)
        return instantiator.instantiate(arguments)
    }

    private fun createNamedTypeList(vararg types: Class<*>): NamedTypeList =
            NamedTypeList.builder(types.size).apply {
                for (i in types.indices)
                    add("name" + i, types[i])
            }.build()

    private fun assertAssignable(target: Class<*>, source: Class<*>) {
        assertTrue(TypeUtils.isAssignable(target, source))
    }

    class InaccessibleConstructor private constructor(@Suppress("UNUSED_PARAMETER") x: Int)
}
