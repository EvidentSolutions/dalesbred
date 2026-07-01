package org.dalesbred

import org.dalesbred.transaction.Isolation
import org.dalesbred.transaction.Propagation
import org.dalesbred.transaction.TransactionSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class TransactionSettingsTest {

    @Test
    fun `sensible toString`() {
        val settings = TransactionSettings().apply {
            propagation = Propagation.REQUIRED
            isolation = Isolation.REPEATABLE_READ
        }

        assertEquals("[propagation=REQUIRED, isolation=REPEATABLE_READ]", settings.toString())
    }
}
