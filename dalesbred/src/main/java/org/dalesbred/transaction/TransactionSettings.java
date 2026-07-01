package org.dalesbred.transaction;

import org.dalesbred.Database;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Contains all the settings that can be configured for individual-transactions.
 *
 * @see Database#withTransaction(TransactionSettings, TransactionCallback)
 */
public final class TransactionSettings {

    private @NotNull Propagation propagation = Propagation.REQUIRED;

    private @NotNull Isolation isolation = Isolation.DEFAULT;

    public @NotNull Propagation getPropagation() {
        return propagation;
    }

    /**
     * Sets the default transaction propagation to use.
     */
    public void setPropagation(@NotNull Propagation propagation) {
        this.propagation = requireNonNull(propagation);
    }

    public @NotNull Isolation getIsolation() {
        return isolation;
    }

    /**
     * Sets the isolation level to use.
     */
    public void setIsolation(@NotNull Isolation isolation) {
        this.isolation = isolation;
    }

    @Override
    public @NotNull String toString() {
        return "[propagation=" + propagation + ", isolation=" + isolation + ']';
    }
}
