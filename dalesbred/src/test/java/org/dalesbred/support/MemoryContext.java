/*
 * Copyright (c) 2015 Evident Solutions Oy
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

package org.dalesbred.support;

import org.jetbrains.annotations.NotNull;

import javax.naming.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public final class MemoryContext implements Context {

    private final Map<String, Object> map = new HashMap<String, Object>();

    @Override
    public Object lookup(@NotNull Name name) {
        return lookup(name.toString());
    }

    @Override
    public Object lookup(@NotNull String name) {
        return map.get(name);
    }

    @Override
    public void bind(@NotNull Name name, Object obj) {
        bind(name.toString(), obj);
    }

    @Override
    public void bind(@NotNull String name, Object obj) {
        map.put(name, obj);
    }

    @Override
    public void rebind(@NotNull Name name, Object obj) {
        rebind(name.toString(), obj);
    }

    @Override
    public void rebind(@NotNull String name, Object obj) {
        map.put(name, obj);
    }

    @Override
    public void unbind(@NotNull Name name) {
        unbind(name.toString());
    }

    @Override
    public void unbind(@NotNull String name) {
        map.remove(name);
    }

    @Override
    public void rename(@NotNull Name oldName, @NotNull Name newName) {
        rename(oldName.toString(), newName.toString());
    }

    @Override
    public void rename(@NotNull String oldName, String newName) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public NamingEnumeration<NameClassPair> list(@NotNull Name name) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public NamingEnumeration<NameClassPair> list(@NotNull String name) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public NamingEnumeration<Binding> listBindings(@NotNull Name name) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public NamingEnumeration<Binding> listBindings(@NotNull String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroySubcontext(@NotNull Name name) {
    }

    @Override
    public void destroySubcontext(@NotNull String name) {
    }

    @NotNull
    @Override
    public Context createSubcontext(@NotNull Name name) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Context createSubcontext(@NotNull String name) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Object lookupLink(@NotNull Name name) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Object lookupLink(@NotNull String name) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public NameParser getNameParser(@NotNull Name name) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public NameParser getNameParser(@NotNull String name) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Name composeName(@NotNull Name name, Name prefix) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public String composeName(String name, String prefix) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Object addToEnvironment(String propName, @NotNull Object propVal) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Object removeFromEnvironment(String propName) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Hashtable<?, ?> getEnvironment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
    }

    @NotNull
    @Override
    public String getNameInNamespace() {
        throw new UnsupportedOperationException();
    }
}
