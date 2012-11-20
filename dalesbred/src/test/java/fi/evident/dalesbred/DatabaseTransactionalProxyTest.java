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

import fi.evident.dalesbred.testutils.LoggingController;
import fi.evident.dalesbred.testutils.SuppressLogging;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class DatabaseTransactionalProxyTest {

    private final Database db = TestDatabaseProvider.createTestDatabase();

    @Rule
    public final LoggingController loggingController = new LoggingController();

    @Test
    public void serviceWithTransactionalMethods() {
        ServiceWithTransactionalMethods service = db.createTransactionalProxyFor(ServiceWithTransactionalMethods.class, new ServiceWithTransactionalMethods() {
            @Override
            public int transactionalMethod() {
                assertInTransaction();
                return 1;
            }

            @Override
            public int nonTransactionalMethod() {
                assertNotInTransaction();
                return 2;
            }
        });

        assertThat(service.transactionalMethod(), is(1));
        assertThat(service.nonTransactionalMethod(), is(2));
    }

    @Test
    public void transactionalService() {
        TransactionalService service = db.createTransactionalProxyFor(TransactionalService.class, new TransactionalService() {
            @Override
            public int methodWithoutSpecificSettings() {
                assertInTransaction();
                return 3;
            }

            @Override
            public void methodWithMandatoryPropagation() {
                fail("we shouldn't end up here");
            }
        });

        assertThat(service.methodWithoutSpecificSettings(), is(3));

        try {
            service.methodWithMandatoryPropagation();
            fail("Expected exception");
        } catch (NoActiveTransactionException e) {
        }
    }

    @Test
    public void overridingTransactionAttributesInTargetDefinition() {
        ServiceWithTransactionalMethods service = db.createTransactionalProxyFor(ServiceWithTransactionalMethods.class, new ServiceWithTransactionalMethods() {
            @Override
            @Transactional(propagation = Propagation.MANDATORY)
            public int transactionalMethod() {
                fail("we shouldn't end up here");
                return 0;
            }

            @Override
            @Transactional
            public int nonTransactionalMethod() {
                assertInTransaction();
                return 4;
            }
        });

        assertThat(service.nonTransactionalMethod(), is(4));

        try {
            service.transactionalMethod();
            fail("Expected exception");
        } catch (NoActiveTransactionException e) {
        }
    }

    @Test
    @SuppressLogging
    public void uncheckedExceptionsAreThrownThroughUnchanged() {
        ServiceWithTransactionalMethods service = db.createTransactionalProxyFor(ServiceWithTransactionalMethods.class, new ServiceWithTransactionalMethods() {
            @Override
            public int transactionalMethod() {
                throw new IllegalArgumentException();
            }

            @Override
            public int nonTransactionalMethod() {
                throw new IllegalStateException();
            }
        });

        try {
            service.transactionalMethod();
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        try {
            service.nonTransactionalMethod();
            fail("expected IllegalStateException");
        } catch (IllegalStateException e) {
        }
    }

    @Test
    @SuppressLogging
    public void checkedExceptionsAreThrownThroughUnchanged() {
        ServiceWithCheckedExceptions service = db.createTransactionalProxyFor(ServiceWithCheckedExceptions.class, new ServiceWithCheckedExceptions() {
            @Override
            public void transactionalMethod() throws IOException {
                throw new IOException();
            }

            @Override
            public void nonTransactionalMethod() throws IOException {
                throw new IOException();
            }
        });

        try {
            service.transactionalMethod();
            fail("Expected IOException");
        } catch (IOException e) {
        }

        try {
            service.nonTransactionalMethod();
            fail("Expected IOException");
        } catch (IOException e) {
        }
    }

    private void assertInTransaction() {
        assertThat("active transaction", db.hasActiveTransaction(), is(true));
    }

    private void assertNotInTransaction() {
        assertThat("active transaction", db.hasActiveTransaction(), is(false));
    }

    public interface ServiceWithTransactionalMethods {
        @Transactional
        int transactionalMethod();

        int nonTransactionalMethod();
    }

    @Transactional
    public interface TransactionalService {
        int methodWithoutSpecificSettings();

        @Transactional(propagation = Propagation.MANDATORY)
        void methodWithMandatoryPropagation();
    }

    public interface ServiceWithCheckedExceptions {

        @Transactional
        void transactionalMethod() throws IOException;

        void nonTransactionalMethod() throws IOException;
    }
}
