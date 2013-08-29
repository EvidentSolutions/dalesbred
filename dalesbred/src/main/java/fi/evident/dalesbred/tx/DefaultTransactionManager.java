/*
 * Copyright (c) 2013 Evident Solutions Oy
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
package fi.evident.dalesbred.tx;

import fi.evident.dalesbred.*;
import fi.evident.dalesbred.connection.ConnectionProvider;
import fi.evident.dalesbred.dialects.Dialect;
import org.jetbrains.annotations.NotNull;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

/**
 * Default transaction manager that handles all transactions by itself.
 */
public final class DefaultTransactionManager implements TransactionManager {

    /** The current active transaction of this thread, or null */
    @NotNull
    private final ThreadLocal<DefaultTransaction> activeTransaction = new ThreadLocal<DefaultTransaction>();

    @NotNull
    private final ConnectionProvider connectionProvider;

    /** The isolation level to use for transactions that have not specified an explicit level. */
    @NotNull
    private Isolation defaultIsolation = Isolation.DEFAULT;

    /** The default propagation for transactions */
    @NotNull
    private Propagation defaultPropagation = Propagation.DEFAULT;

    public DefaultTransactionManager(@NotNull ConnectionProvider connectionProvider) {
        this.connectionProvider = requireNonNull(connectionProvider);
    }

    @Override
    public <T> T withTransaction(@NotNull TransactionSettings settings, @NotNull TransactionCallback<T> callback, @NotNull Dialect dialect) {

        Propagation propagation = settings.getPropagation().normalize(defaultPropagation);
        Isolation isolation = settings.getIsolation().normalize(defaultIsolation);
        int retries = settings.getRetries();

        DefaultTransaction existingTransaction = activeTransaction.get();

        if (existingTransaction != null) {
            if (propagation == Propagation.REQUIRES_NEW)
                return withSuspendedTransaction(isolation, callback, dialect);
            else if (propagation == Propagation.NESTED)
                return existingTransaction.nested(retries, callback);
            else
                return existingTransaction.join(callback);

        } else {
            if (propagation == Propagation.MANDATORY)
                throw new NoActiveTransactionException("Transaction propagation was MANDATORY, but there was no existing transaction.");

            DefaultTransaction newTransaction = new DefaultTransaction(connectionProvider, dialect, isolation);
            try {
                activeTransaction.set(newTransaction);
                return newTransaction.execute(retries, callback);
            } finally {
                activeTransaction.set(null);
                newTransaction.close();
            }
        }
    }

    @Override
    public <T> T withCurrentTransaction(@NotNull TransactionCallback<T> callback, @NotNull Dialect dialect) {
        DefaultTransaction transaction = activeTransaction.get();
        if (transaction != null)
            return transaction.join(callback);
        else
            throw new NoActiveTransactionException("Tried to perform database operation without active transaction. Database accesses should be bracketed with Database.withTransaction(...) or implicit transactions should be enabled.");
    }

    @Override
    public boolean hasActiveTransaction() {
        return activeTransaction.get() != null;
    }

    private <T> T withSuspendedTransaction(@NotNull Isolation isolation, @NotNull TransactionCallback<T> callback, @NotNull Dialect dialect) {
        DefaultTransaction suspended = activeTransaction.get();
        try {
            activeTransaction.set(null);

            TransactionSettings settings = new TransactionSettings();
            settings.setPropagation(Propagation.REQUIRED);
            settings.setIsolation(isolation);
            return withTransaction(settings, callback, dialect);
        } finally {
            activeTransaction.set(suspended);
        }
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
