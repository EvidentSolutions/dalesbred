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

package fi.evident.dalesbred.support.aopalliance;

import fi.evident.dalesbred.*;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Provider;
import java.lang.reflect.Method;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

/**
 * An AOP Alliance compatible interceptor.
 *
 * @see fi.evident.dalesbred.support.guice.GuiceSupport
 */
public final class AopAllianceTransactionalMethodInterceptor implements MethodInterceptor {

    @Nullable
    private final Provider<Database> databaseProvider;

    /**
     * Constructs the interceptor with given database-provider.
     */
    public AopAllianceTransactionalMethodInterceptor(@NotNull Provider<Database> databaseProvider) {
        this.databaseProvider = requireNonNull(databaseProvider);
    }

    @Override
    @Nullable
    public Object invoke(@NotNull final MethodInvocation invocation) throws Throwable {
        try {
            TransactionSettings settings = getTransactionSettings(invocation);
            return databaseProvider.get().withTransaction(settings, new TransactionCallback<Object>() {
                @Override
                public Object execute(@NotNull TransactionContext tx) {
                    try {
                        return invocation.proceed();
                    } catch (Throwable e) {
                        throw new WrappedException(e);
                    }
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
