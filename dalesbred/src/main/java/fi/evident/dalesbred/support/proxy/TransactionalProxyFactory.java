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

package fi.evident.dalesbred.support.proxy;

import fi.evident.dalesbred.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

/**
 * Creates proxies for transactional services.
 */
public final class TransactionalProxyFactory {

    private TransactionalProxyFactory() { }

    /**
     * Returns a new transactional proxy for given target object.
     */
    public static <T> T createTransactionalProxyFor(@NotNull Database db, @NotNull final Class<T> iface, @NotNull final T target) {
        return iface.cast(createTransactionalProxyFor(db, iface.getClassLoader(), new Class<?>[]{iface}, target));
    }

    /**
     * Returns a new transactional proxy for given target object. The proxy will implement all of the
     * given interfaces.
     */
    public static Object createTransactionalProxyFor(@NotNull final Database db, @NotNull ClassLoader classLoader, @NotNull final Class<?>[] interfaces, @NotNull final Object target) {
        return Proxy.newProxyInstance(classLoader, interfaces, new TransactionInvocationHandler(db, target));
    }

    private static final class TransactionInvocationHandler implements InvocationHandler {

        private final Database db;
        private final Object target;

        public TransactionInvocationHandler(@NotNull Database db, @NotNull Object target) {
            this.db = requireNonNull(db);
            this.target = requireNonNull(target);
        }

        @Override
        public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
            TransactionSettings tx = findTransactionSettings(method);

            if (tx == null)
                return invokeWithoutTransaction(method, args);
            else
                return invokeInTransaction(tx, method, args);
        }

        private Object invokeWithoutTransaction(Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

        private Object invokeInTransaction(@NotNull TransactionSettings settings,
                                           @NotNull final Method method,
                                           final Object[] args) throws Throwable {
            try {
                return db.withTransaction(settings, new TransactionCallback<Object>() {
                    @Override
                    public Object execute(@NotNull TransactionContext tx) throws SQLException {
                        try {
                            return method.invoke(target, args);
                        } catch (IllegalAccessException e) {
                            throw new WrappedException(e);
                        } catch (InvocationTargetException e) {
                            throw new WrappedException(e.getTargetException());
                        }
                    }
                });
            } catch (WrappedException e) {
                throw e.throwable;
            }
        }

        @Nullable
        private TransactionSettings findTransactionSettings(@NotNull Method interfaceMethod) {
            Transactional tx = findTransactionDefinition(interfaceMethod);
            if (tx != null) {
                TransactionSettings settings = new TransactionSettings();
                settings.setPropagation(tx.propagation());
                settings.setIsolation(tx.isolation());
                return settings;

            } else {
                return null;
            }
        }

        @Nullable
        private Transactional findTransactionDefinition(@NotNull Method interfaceMethod) {
            try {
                Method actualMethod = target.getClass().getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes());
                Transactional tx = actualMethod.getAnnotation(Transactional.class);

                if (tx == null)
                    tx = interfaceMethod.getAnnotation(Transactional.class);

                if (tx == null)
                    tx = interfaceMethod.getDeclaringClass().getAnnotation(Transactional.class);

                return tx;

            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class WrappedException extends RuntimeException {
        final Throwable throwable;

        WrappedException(Throwable throwable) {
            this.throwable = throwable;
        }
    }
}
