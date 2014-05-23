/*
 * Copyright (c) 2014 Evident Solutions Oy
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

package fi.evident.dalesbred.build;

import groovy.lang.Closure;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("NullableProblems")
public final class LazyMap<K,V> extends AbstractMap<K,V> implements Serializable {

    @SuppressWarnings("TransientFieldNotInitialized")
    private transient Closure<Map<K,V>> closure;
    private Map<K,V> delegate;

    private static final long serialVersionUID = 7416144329250836789L;

    public LazyMap(Closure<Map<K,V>> closure) {
        this.closure = closure;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return getDelegate().entrySet();
    }

    @Override
    public int size() {
        return getDelegate().size();
    }

    @Override
    public V get(Object key) {
        return getDelegate().get(key);
    }

    @Override
    public Set<K> keySet() {
        return getDelegate().keySet();
    }

    @Override
    public boolean containsValue(Object value) {
        return getDelegate().containsValue(value);
    }

    @Override
    public boolean containsKey(Object key) {
        return getDelegate().containsKey(key);
    }

    private Map<K,V> getDelegate() {
        if (closure != null) {
            delegate = closure.call();
            //noinspection AssignmentToNull
            closure = null;
        }
        return delegate;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        getDelegate(); // force closure to be resolved

        out.defaultWriteObject();
    }
}
