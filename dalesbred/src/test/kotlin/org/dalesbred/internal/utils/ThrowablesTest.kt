package org.dalesbred.internal.utils

import java.io.IOException
import java.sql.SQLException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class ThrowablesTest {

    @Test
    fun `propagating runtime exception returns it`() {
        val exception = RuntimeException()
        assertEquals(exception, Throwables.propagate(exception))
    }

    @Test
    fun `propagating checked exception wraps it into runtime exception`() {
        val exception = Exception()

        val propagated = Throwables.propagate(exception)
        @Suppress("USELESS_IS_CHECK")
        assertTrue { propagated is RuntimeException }
        assertEquals(exception, propagated.cause)
    }

    @Test
    fun `propagating error throws it`() {
        val error = MyError()

        try {
            Throwables.propagate(error)
            fail("Expected Error")
        } catch (e: MyError) {
            assertEquals(error, e)
        }
    }

    @Test
    fun `propagating allowed checked exception returns it`() {
        val exception = IOException()
        assertEquals(exception, Throwables.propagate(exception, IOException::class.java))
    }

    @Test
    fun `propagating disallowed exception throws it wrapped`() {
        val exception = SQLException()

        try {
            throw Throwables.propagate(exception, IOException::class.java)

        } catch (e: RuntimeException) {
            assertEquals(exception, e.cause)
        }

    }

    class MyError : Error()
}
