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

package org.dalesbred.support.spring;

import org.dalesbred.Transactional;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.annotation.TransactionAnnotationParser;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;

import java.lang.reflect.AnnotatedElement;

/**
 * Allows using Dalesbred's {@link Transactional} with Spring. Generally it's preferable
 * to use Spring's own annotations to manage transactions.
 */
public final class DalesbredSpringAnnotationParser implements TransactionAnnotationParser {

    @Override
    @Nullable
    public TransactionAttribute parseTransactionAnnotation(AnnotatedElement ae) {
        Transactional ann = AnnotationUtils.getAnnotation(ae, Transactional.class);
        if (ann != null) {
            return parseTransactionAnnotation(ann);
        } else {
            return null;
        }
    }

    public TransactionAttribute parseTransactionAnnotation(Transactional ann) {
        RuleBasedTransactionAttribute attribute = new RuleBasedTransactionAttribute();

        attribute.setPropagationBehavior(SpringTransactionManager.springPropagationCode(ann.propagation()));
        attribute.setIsolationLevel(SpringTransactionManager.springIsolationCode(ann.isolation()));

        if (ann.retries() != 0)
            throw new IllegalStateException("Retries not supported for Spring-managed transactions.");

        return attribute;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DalesbredSpringAnnotationParser;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
