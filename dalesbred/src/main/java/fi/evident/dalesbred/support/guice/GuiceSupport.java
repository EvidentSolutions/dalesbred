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

package fi.evident.dalesbred.support.guice;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Provider;
import fi.evident.dalesbred.Database;
import fi.evident.dalesbred.Transactional;
import fi.evident.dalesbred.support.aopalliance.AopAllianceTransactionalMethodInterceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.jetbrains.annotations.NotNull;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;

/**
 * Provides methods for supporting Guice.
 */
public final class GuiceSupport {

    private GuiceSupport() { }

    /**
     * Binds transaction interceptor for all methods or classes that are annotated with {@link Transactional}.
     */
    public static void bindTransactionInterceptor(@NotNull Binder binder, @NotNull Key<Database> databaseKey) {
        Provider<Database> databaseProvider = binder.getProvider(databaseKey);
        MethodInterceptor interceptor = new AopAllianceTransactionalMethodInterceptor(databaseProvider);

        binder.bindInterceptor(any(), annotatedWith(Transactional.class), interceptor);
        binder.bindInterceptor(annotatedWith(Transactional.class), any(), interceptor);
    }
}
