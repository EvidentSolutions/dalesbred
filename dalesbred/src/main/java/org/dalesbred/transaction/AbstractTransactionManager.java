package org.dalesbred.transaction;

import org.dalesbred.dialect.Dialect;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class AbstractTransactionManager implements TransactionManager {

    protected abstract @NotNull Optional<DefaultTransaction> getActiveTransaction();

    protected abstract <T> T withNewTransaction(@NotNull TransactionCallback<T> callback,
                                                @NotNull Dialect dialect,
                                                @NotNull Isolation isolation);

    protected abstract <T> T withSuspendedTransaction(@NotNull TransactionCallback<T> callback,
                                                      @NotNull Isolation isolation,
                                                      @NotNull Dialect dialect);

    @Override
    public <T> T withTransaction(@NotNull TransactionSettings settings, @NotNull TransactionCallback<T> callback, @NotNull Dialect dialect) {
        Propagation propagation = settings.getPropagation();
        Isolation isolation = settings.getIsolation();

        DefaultTransaction existingTransaction = getActiveTransaction().orElse(null);

        if (existingTransaction != null) {
            if (propagation == Propagation.REQUIRES_NEW)
                return withSuspendedTransaction(callback, isolation, dialect);
            else if (propagation == Propagation.NESTED)
                return existingTransaction.nested(callback, dialect);
            else
                return existingTransaction.join(callback, dialect);

        } else {
            if (propagation == Propagation.MANDATORY)
                throw new NoActiveTransactionException("Transaction propagation was MANDATORY, but there was no existing transaction.");

            return withNewTransaction(callback, dialect, isolation);
        }
    }

    @Override
    public <T> T withCurrentTransaction(@NotNull TransactionCallback<T> callback, @NotNull Dialect dialect) {
        DefaultTransaction transaction = getActiveTransaction().orElseThrow(() ->
                new NoActiveTransactionException("Tried to perform database operation without active transaction. Database accesses should be bracketed with Database.withTransaction(...) or implicit transactions should be enabled."));
        return transaction.join(callback, dialect);
    }

    @Override
    public boolean hasActiveTransaction() {
        return getActiveTransaction().isPresent();
    }
}
