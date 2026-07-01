package org.dalesbred.testutils

/**
 * Executes block of code with given value of given system-property.
 */
fun withSystemProperty(property: String, value: String, block: () -> Unit) {
    val old = System.getProperty(property)
    try {
        setSystemProperty(property, value)
        block()

    } finally {
        setSystemProperty(property, old)
    }
}

private fun setSystemProperty(name: String, value: String?) {
    if (value != null)
        System.setProperty(name, value)
    else
        System.clearProperty(name)
}
