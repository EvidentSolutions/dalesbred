/*
 * Copyright (c) 2012 Evident Solutions Oy
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

package fi.evident.dalesbred.support;

import javax.naming.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public final class MemoryContext implements Context {

    private final Map<String, Object> map = new HashMap<String, Object>();

    @Override
    public Object lookup(Name name) {
        return lookup(name.toString());
    }

    @Override
    public Object lookup(String name) {
        return map.get(name);
    }

    @Override
    public void bind(Name name, Object obj) {
        bind(name.toString(), obj);
    }

    @Override
    public void bind(String name, Object obj) {
        map.put(name, obj);
    }

    @Override
    public void rebind(Name name, Object obj) {
        rebind(name.toString(), obj);
    }

    @Override
    public void rebind(String name, Object obj) {
        map.put(name, obj);
    }

    @Override
    public void unbind(Name name) {
        unbind(name.toString());
    }

    @Override
    public void unbind(String name) {
        map.remove(name);
    }

    @Override
    public void rename(Name oldName, Name newName) {
        rename(oldName.toString(), newName.toString());
    }

    @Override
    public void rename(String oldName, String newName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NamingEnumeration<NameClassPair> list(Name name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NamingEnumeration<Binding> listBindings(Name name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NamingEnumeration<Binding> listBindings(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroySubcontext(Name name) {
    }

    @Override
    public void destroySubcontext(String name) {
    }

    @Override
    public Context createSubcontext(Name name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Context createSubcontext(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object lookupLink(Name name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object lookupLink(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NameParser getNameParser(Name name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NameParser getNameParser(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Name composeName(Name name, Name prefix) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String composeName(String name, String prefix) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object addToEnvironment(String propName, Object propVal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object removeFromEnvironment(String propName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Hashtable<?, ?> getEnvironment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
    }

    @Override
    public String getNameInNamespace() {
        throw new UnsupportedOperationException();
    }
}
