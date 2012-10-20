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

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

/**
 * Provides transactions with access to the context.
 */
public final class TransactionContext {

    @NotNull
    private final Connection connection;
    private boolean rollbackOnly = false;

    TransactionContext(@NotNull Connection connection) {
        this.connection = requireNonNull(connection);
    }

    /**
     * Returns the raw JDBC-connection for this transaction.
     */
    @NotNull
    public Connection getConnection() {
        return connection;
    }

    /**
     * Requests that this transaction will be rolled back.
     */
    public void setRollbackOnly() {
        rollbackOnly = true;
    }

    public boolean isRollbackOnly() {
        return rollbackOnly;
    }
}
