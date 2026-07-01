package org.dalesbred.internal.utils

import org.dalesbred.internal.utils.OptionalUtils.unwrapOptionalAsNull
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OptionalUtilsTest {

    @Test
    fun `unwrapping null returns null`() {
        assertNull(unwrapOptionalAsNull(null))
    }

    @Test
    fun `unwrapping non optionals returns value as it is`() {
        assertEquals("foo", unwrapOptionalAsNull("foo"))
        assertEquals(42, unwrapOptionalAsNull(42))
    }

    @Test
    fun `unwrapping empty optional types returns null`() {
        assertNull(unwrapOptionalAsNull(Optional.empty<Any>()))
        assertNull(unwrapOptionalAsNull(OptionalInt.empty()))
        assertNull(unwrapOptionalAsNull(OptionalDouble.empty()))
        assertNull(unwrapOptionalAsNull(OptionalLong.empty()))
    }

    @Test
    fun `unwrapping non empty optional types returns contained value`() {
        assertEquals("foo", unwrapOptionalAsNull(Optional.of("foo")))
        assertEquals(42, unwrapOptionalAsNull(Optional.of(42)))
        assertEquals(42, unwrapOptionalAsNull(OptionalInt.of(42)))
        assertEquals(42.2, unwrapOptionalAsNull(OptionalDouble.of(42.2)))
        assertEquals(42L, unwrapOptionalAsNull(OptionalLong.of(42L)))
    }
}
