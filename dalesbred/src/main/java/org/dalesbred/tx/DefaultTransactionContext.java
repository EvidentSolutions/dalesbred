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

import org.dalesbred.TransactionContext;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

import static org.dalesbred.utils.Require.requireNonNull;

final class DefaultTransactionContext extends TransactionContext {

    @NotNull
    private final Connection connection;
    private boolean rollbackOnly = false;

    DefaultTransactionContext(@NotNull Connection connection) {
        this.connection = requireNonNull(connection);
    }

    /**
     * Returns the raw JDBC-connection for this transaction.
     */
    @Override
    @NotNull
    public Connection getConnection() {
        return connection;
    }

    /**
     * Requests that this transaction will be rolled back.
     */
    @Override
    public void setRollbackOnly() {
        rollbackOnly = true;
    }

    @Override
    public boolean isRollbackOnly() {
        return rollbackOnly;
    }
}
