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

package fi.evident.dalesbred;

/**
 * Transaction propagation types.
 */
public enum Propagation {

    /** Use the default propagation level that is configured */
    DEFAULT,

    /** Join existing transaction if there is one, otherwise create a new one. */
    REQUIRED,

    /** Join existing transaction if there is one, otherwise throw an exception. */
    MANDATORY,

    /** Always create a new transaction. Existing transaction is suspended for the duration of this transaction. */
    REQUIRES_NEW,

    /** Start a nested transaction if there is a current transaction, otherwise start a new normal transaction. */
    NESTED,;

    Propagation normalize(Propagation defaultValue) {
        return (this != DEFAULT)         ? this
             : (defaultValue != DEFAULT) ? defaultValue
             : REQUIRED;
    }
}
