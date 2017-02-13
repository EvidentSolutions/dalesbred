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

package org.dalesbred.integration

import javax.naming.Context
import javax.naming.Name

class MemoryContext : Context {

    private val map = mutableMapOf<String, Any>()

    override fun lookup(name: Name): Any? = lookup(name.toString())

    override fun lookup(name: String): Any? = map[name]

    override fun bind(name: Name, obj: Any) {
        bind(name.toString(), obj)
    }

    override fun bind(name: String, obj: Any) {
        map.put(name, obj)
    }

    override fun rebind(name: Name, obj: Any) {
        rebind(name.toString(), obj)
    }

    override fun rebind(name: String, obj: Any) {
        map.put(name, obj)
    }

    override fun unbind(name: Name) {
        unbind(name.toString())
    }

    override fun unbind(name: String) {
        map.remove(name)
    }

    override fun rename(oldName: Name, newName: Name) {
        rename(oldName.toString(), newName.toString())
    }

    override fun close() {}

    override fun rename(oldName: String, newName: String) = throw UnsupportedOperationException()

    override fun list(name: Name) = throw UnsupportedOperationException()

    override fun list(name: String) = throw UnsupportedOperationException()

    override fun listBindings(name: Name) = throw UnsupportedOperationException()

    override fun listBindings(name: String) = throw UnsupportedOperationException()

    override fun destroySubcontext(name: Name) = throw UnsupportedOperationException()

    override fun destroySubcontext(name: String) = throw UnsupportedOperationException()

    override fun createSubcontext(name: Name) = throw UnsupportedOperationException()

    override fun createSubcontext(name: String) = throw UnsupportedOperationException()

    override fun lookupLink(name: Name) = throw UnsupportedOperationException()

    override fun lookupLink(name: String) = throw UnsupportedOperationException()

    override fun getNameParser(name: Name) = throw UnsupportedOperationException()

    override fun getNameParser(name: String) = throw UnsupportedOperationException()

    override fun composeName(name: Name, prefix: Name) = throw UnsupportedOperationException()

    override fun composeName(name: String, prefix: String) = throw UnsupportedOperationException()

    override fun addToEnvironment(propName: String, propVal: Any) = throw UnsupportedOperationException()

    override fun removeFromEnvironment(propName: String) = throw UnsupportedOperationException()

    override fun getEnvironment() = throw UnsupportedOperationException()

    override fun getNameInNamespace() = throw UnsupportedOperationException()
}
