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

package org.dalesbred.transaction;

import org.dalesbred.Database;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

/**
 * Callback for executing a block of code within a transaction.
 *
 * @see Database#withTransaction(TransactionCallback)
 * @see VoidTransactionCallback
 */
@SuppressWarnings({"UnusedParameters", "SameReturnValue"})
@FunctionalInterface
public interface TransactionCallback<T> {
    T execute(@NotNull TransactionContext tx) throws SQLException;

    /**
     * Converts {@code VoidTransactionCallback} to {@code TransactionCallback<Void>}
     */
    static @NotNull TransactionCallback<Void> fromVoidCallback(@NotNull VoidTransactionCallback callback) {
        return tx -> {
            callback.execute(tx);
            //noinspection ReturnOfNull
            return null;
        };
    }
}
