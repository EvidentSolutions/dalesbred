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

package org.dalesbred.transaction;

import org.dalesbred.dialects.Dialect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractTransactionManager implements TransactionManager {

    /**
     * The isolation level to use for transactions that have not specified an explicit level.
     */
    @NotNull
    private Isolation defaultIsolation = Isolation.DEFAULT;

    /**
     * The default propagation for transactions
     */
    @NotNull
    private Propagation defaultPropagation = Propagation.DEFAULT;

    @Nullable
    protected abstract DefaultTransaction getActiveTransaction();

    protected abstract <T> T withNewTransaction(@NotNull TransactionCallback<T> callback,
                                                @NotNull Dialect dialect,
                                                @NotNull Isolation isolation,
                                                int retries);

    protected abstract <T> T withSuspendedTransaction(@NotNull TransactionCallback<T> callback,
                                                      @NotNull Isolation isolation,
                                                      @NotNull Dialect dialect,
                                                      int retries);

    @Override
    public <T> T withTransaction(@NotNull TransactionSettings settings, @NotNull TransactionCallback<T> callback, @NotNull Dialect dialect) {
        Propagation propagation = settings.getPropagation().normalize(defaultPropagation);
        Isolation isolation = settings.getIsolation().normalize(defaultIsolation);
        int retries = settings.getRetries();

        DefaultTransaction existingTransaction = getActiveTransaction();

        if (existingTransaction != null) {
            if (propagation == Propagation.REQUIRES_NEW)
                return withSuspendedTransaction(callback, isolation, dialect, retries);
            else if (propagation == Propagation.NESTED)
                return existingTransaction.nested(retries, callback, dialect);
            else
                return existingTransaction.join(callback, dialect);

        } else {
            if (propagation == Propagation.MANDATORY)
                throw new NoActiveTransactionException("Transaction propagation was MANDATORY, but there was no existing transaction.");

            return withNewTransaction(callback, dialect, isolation, retries);
        }
    }

    @Override
    public <T> T withCurrentTransaction(@NotNull TransactionCallback<T> callback, @NotNull Dialect dialect) {
        DefaultTransaction transaction = getActiveTransaction();
        if (transaction != null)
            return transaction.join(callback, dialect);
        else
            throw new NoActiveTransactionException("Tried to perform database operation without active transaction. Database accesses should be bracketed with Database.withTransaction(...) or implicit transactions should be enabled.");
    }

    @Override
    public boolean hasActiveTransaction() {
        return getActiveTransaction() != null;
    }

    @Override
    @NotNull
    public Isolation getDefaultIsolation() {
        return defaultIsolation;
    }

    @Override
    public void setDefaultIsolation(@NotNull Isolation isolation) {
        this.defaultIsolation = isolation;
    }

    @Override
    @NotNull
    public Propagation getDefaultPropagation() {
        return defaultPropagation;
    }

    @Override
    public void setDefaultPropagation(@NotNull Propagation propagation) {
        this.defaultPropagation = propagation;
    }
}
