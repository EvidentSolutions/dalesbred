package org.dalesbred.internal.utils

import org.dalesbred.DatabaseException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EnumUtilsTest {

    private enum class TestEnum {
        FOO, BAR, BAZ
    }

    @Test
    fun `enumByOrdinal valid`() {
        assertEquals(TestEnum.FOO, EnumUtils.enumByOrdinal(TestEnum::class.java, 0))
        assertEquals(TestEnum.BAR, EnumUtils.enumByOrdinal(TestEnum::class.java, 1))
        assertEquals(TestEnum.BAZ, EnumUtils.enumByOrdinal(TestEnum::class.java, 2))
    }

    @Test
    fun `enumByOrdinal invalid`() {
        assertFailsWith<DatabaseException> {
            EnumUtils.enumByOrdinal(TestEnum::class.java, 4)
        }
    }
}
