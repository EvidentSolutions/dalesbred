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
import org.junit.Test;

import java.lang.annotation.Annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class TransactionSettingsTest {

    @Test
    public void sensibleToString() {
        TransactionSettings settings = new TransactionSettings();

        settings.setPropagation(Propagation.REQUIRED);
        settings.setIsolation(Isolation.REPEATABLE_READ);
        settings.setRetries(3);

        assertEquals("[propagation=REQUIRED, isolation=REPEATABLE_READ, retries=3]", settings.toString());
    }

    @Test(expected=IllegalArgumentException.class)
    public void negativeRetries() {
        new TransactionSettings().setRetries(-1);
    }

    @Test
    public void initializeFromAnnotation() {
        MyTransactional transactional = new MyTransactional();
        transactional.isolation = Isolation.SERIALIZABLE;
        transactional.propagation = Propagation.NESTED;
        transactional.retries = 4;

        TransactionSettings settings = TransactionSettings.fromAnnotation(transactional);
        assertSame(transactional.isolation(), settings.getIsolation());
        assertSame(transactional.propagation(), settings.getPropagation());
        assertSame(transactional.retries(), settings.getRetries());
    }

    @SuppressWarnings("ClassExplicitlyAnnotation")
    private static final class MyTransactional implements Transactional {
        Isolation isolation = Isolation.REPEATABLE_READ;
        Propagation propagation = Propagation.REQUIRED;
        int retries = 0;

        @NotNull
        @Override
        public Propagation propagation() {
            return propagation;
        }

        @NotNull
        @Override
        public Isolation isolation() {
            return isolation;
        }

        @Override
        public int retries() {
            return retries;
        }

        @NotNull
        @Override
        public Class<? extends Annotation> annotationType() {
            return Transactional.class;
        }
    }
}
