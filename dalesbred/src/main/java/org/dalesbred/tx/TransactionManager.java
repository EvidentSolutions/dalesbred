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
package org.dalesbred.tx;

import org.dalesbred.Isolation;
import org.dalesbred.Propagation;
import org.dalesbred.TransactionCallback;
import org.dalesbred.TransactionSettings;
import org.dalesbred.dialects.Dialect;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract the mechanism in which transactions are handled.
 */
public interface TransactionManager {

    /**
     * Executes given callback with given transaction settings.
     */
    <T> T withTransaction(@NotNull TransactionSettings settings, @NotNull TransactionCallback<T> callback, @NotNull Dialect dialect);

    /**
     * Executes given callback within current transaction.
     */
    <T> T withCurrentTransaction(@NotNull TransactionCallback<T> callback, @NotNull Dialect dialect);

    /**
     * Returns true if the code is executing inside transaction.
     */
    boolean hasActiveTransaction();

    /**
     * Returns the used transaction isolation level.
     */
    @NotNull
    Isolation getDefaultIsolation();

    /**
     * Sets the transaction isolation level to use.
     */
    void setDefaultIsolation(@NotNull Isolation isolation);

    /**
     * Returns the default transaction propagation to use.
     */
    @NotNull
    Propagation getDefaultPropagation();

    /**
     * Returns the default transaction propagation to use.
     */
    void setDefaultPropagation(@NotNull Propagation propagation);
}
