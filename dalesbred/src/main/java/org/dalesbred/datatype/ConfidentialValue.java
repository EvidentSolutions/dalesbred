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

package org.dalesbred.datatype;

import org.jetbrains.annotations.Nullable;

/**
 * Confidential wrapper for objects. You can bind ConfidentialValues as query
 * parameters which will execute th
 * will not reveal their representation in toString, but
 */
public final class ConfidentialValue {

    @Nullable
    private final Object value;

    public ConfidentialValue(@Nullable Object value) {
        this.value = value;
    }

    /**
     * Wraps the value in a confidential wrapper which prevents it from ever being shown
     * in any logs or stack-traces, but it can still be used normally as a query-parameter.
     */
    public static Object confidential(Object value) {
        return new ConfidentialValue(value);
    }

    @Nullable
    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "****";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (obj instanceof ConfidentialValue) {
            ConfidentialValue rhs = (ConfidentialValue) obj;
            return (value == null) ? rhs.value == null : value.equals(rhs.value);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (value != null) ? value.hashCode() : 0;
    }
}
