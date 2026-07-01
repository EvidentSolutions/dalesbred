package org.dalesbred.internal.instantiation.test

object InaccessibleClassRef {

    val INACCESSIBLE_CLASS: Class<*> = InaccessibleClass::class.java
}

private class InaccessibleClass(@Suppress("UNUSED_PARAMETER") x: Int)
