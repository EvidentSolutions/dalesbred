package org.dalesbred.testutils

import java.util.*

inline fun withUTCTimeZone(block: () -> Unit) {
    withTimeZone("UTC", block)
}

inline fun withTimeZone(name: String, block: () -> Unit) {
    val old = TimeZone.getDefault()
    try {
        TimeZone.setDefault(TimeZone.getTimeZone(name))
        block()
    } finally {
        TimeZone.setDefault(old)
    }
}
