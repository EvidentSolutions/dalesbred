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

package org.dalesbred.integration.aopalliance;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.dalesbred.Database;
import org.dalesbred.integration.guice.GuiceSupport;
import org.dalesbred.internal.utils.Require;
import org.dalesbred.transaction.TransactionSettings;
import org.dalesbred.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Provider;
import java.lang.reflect.Method;

/**
 * An AOP Alliance compatible interceptor.
 *
 * @see GuiceSupport
 */
public final class AopAllianceTransactionalMethodInterceptor implements MethodInterceptor {

    @NotNull
    private final Provider<Database> databaseProvider;

    /**
     * Constructs the interceptor with given database-provider.
     */
    public AopAllianceTransactionalMethodInterceptor(@NotNull Provider<Database> databaseProvider) {
        this.databaseProvider = Require.requireNonNull(databaseProvider);
    }

    @Override
    @Nullable
    public Object invoke(@NotNull MethodInvocation invocation) throws Throwable {
        try {
            TransactionSettings settings = getTransactionSettings(invocation);
            return databaseProvider.get().withTransaction(settings, tx -> {
                try {
                    return invocation.proceed();
                } catch (Throwable e) {
                    throw new WrappedException(e);
                }
            });
        } catch (WrappedException e) {
            throw e.getCause();
        }
    }

    @NotNull
    private static TransactionSettings getTransactionSettings(@NotNull MethodInvocation invocation) {
        Transactional tx = findTransactionDefinition(invocation.getMethod());
        if (tx != null)
            return TransactionSettings.fromAnnotation(tx);
        else
            return new TransactionSettings();
    }

    @Nullable
    private static Transactional findTransactionDefinition(@NotNull Method method) {
        Transactional tx = method.getAnnotation(Transactional.class);
        return (tx != null) ? tx : method.getDeclaringClass().getAnnotation(Transactional.class);
    }

    private static class WrappedException extends RuntimeException {
        WrappedException(@NotNull Throwable e) {
            super(e);
        }
    }
}
