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

package org.dalesbred.junit;

import org.dalesbred.Database;
import org.dalesbred.TransactionCallback;
import org.dalesbred.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.inject.Provider;

import static org.dalesbred.junit.TransactionalTests.RollbackPolicy.*;
import static org.dalesbred.utils.Require.requireNonNull;

/**
 * A JUnit {@link TestRule} that can be used to run the test-methods in transactions.
 * <p>
 * To use the rule, create a database and add the following lines to your test:
 * <pre>
 * {@literal @Rule}
 * public final TransactionalTests transactionalTests = new TransactionalTests(db);
 * </pre>
 */
public final class TransactionalTests implements TestRule {

    @NotNull
    private final Provider<Database> db;

    @NotNull
    private final RollbackPolicy rollbackPolicy;

    /**
     * Specified the rollback-policy for tests.
     */
    public enum RollbackPolicy {
        ROLLBACK_ON_FAILURE, ROLLBACK_NEVER, ROLLBACK_ALWAYS
    }

    /**
     * Constructs a rule that will wrap tests in transactions and use the default
     * rollback-policy ({@link RollbackPolicy#ROLLBACK_ON_FAILURE}) for rolling back
     * the transaction.
     */
    public TransactionalTests(@NotNull Database db) {
        this(db, ROLLBACK_ON_FAILURE);
    }

    /**
     * Constructs a rule that will wrap tests in transactions and use the specified
     * rollback-policy for rolling back the transaction.
     */
    public TransactionalTests(@NotNull final Database db, @NotNull RollbackPolicy rollbackPolicy) {
        this(new Provider<Database>() {
            @Override
            public Database get() {
                return db;
            }
        }, rollbackPolicy);
    }

    /**
     * Constructs a rule that will wrap tests in transactions and use the default
     * rollback-policy ({@link RollbackPolicy#ROLLBACK_ON_FAILURE}) for rolling back
     * the transaction.
     */
    public TransactionalTests(@NotNull Provider<Database> db) {
        this(db, ROLLBACK_ON_FAILURE);
    }

    /**
     * Constructs a rule that will wrap tests in transactions and use the specified
     * rollback-policy for rolling back the transaction.
     */
    public TransactionalTests(@NotNull Provider<Database> db, @NotNull RollbackPolicy rollbackPolicy) {
        this.db = requireNonNull(db);
        this.rollbackPolicy = requireNonNull(rollbackPolicy);
    }

    @NotNull
    @Override
    public Statement apply(@NotNull final Statement base, @NotNull Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Throwable throwable =
                    db.get().withTransaction(new TransactionCallback<Throwable>() {
                        @Nullable
                        @Override
                        public Throwable execute(@NotNull TransactionContext tx) {
                            try {
                                base.evaluate();
                                if (rollbackPolicy == ROLLBACK_ALWAYS)
                                    tx.setRollbackOnly();
                                return null;
                            } catch (Throwable e) {
                                if (rollbackPolicy != ROLLBACK_NEVER)
                                    tx.setRollbackOnly();
                                return e;
                            }
                        }
                    });
                if (throwable != null)
                    throw throwable;
            }
        };
    }
}
