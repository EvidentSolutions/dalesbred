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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import fi.evident.dalesbred.*;
import fi.evident.dalesbred.support.guice.DriverManagerDatabaseModule;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.SQLException;

import static com.google.inject.name.Names.named;
import static fi.evident.dalesbred.Isolation.SERIALIZABLE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AopAllianceTransactionalMethodInterceptorTest {

    @Inject
    @Named("myService1")
    private MyService service1;

    @Inject
    @Named("myService2")
    private MyService service2;

    @Test
    public void interceptorCreatesTransaction() {
        assertThat(service1.isExecutedTransactionally(), is(true));
        assertThat(service2.isExecutedTransactionally(), is(true));
    }

    @Test
    public void isolationIsSet() {
        assertThat(service1.getIsolation(), is(SERIALIZABLE));
        assertThat(service2.getIsolation(), is(SERIALIZABLE));
    }

    public interface MyService {
        boolean isExecutedTransactionally();
        Isolation getIsolation();
    }

    public static class MyServiceImplementation implements MyService {

        @Inject
        Database db;

        @Override
        @Transactional
        public boolean isExecutedTransactionally() {
            return db.hasActiveTransaction();
        }

        @Override
        @Transactional(isolation = SERIALIZABLE)
        public Isolation getIsolation() {
            return getTransactionIsolation(db);
        }
    }

    @Transactional(isolation = SERIALIZABLE)
    public static class MyServiceImplementation2 implements MyService {

        @Inject
        Database db;

        @Override
        public boolean isExecutedTransactionally() {
            return db.hasActiveTransaction();
        }

        @Override
        public Isolation getIsolation() {
            return getTransactionIsolation(db);
        }
    }

    private static Isolation getTransactionIsolation(Database db) {
        return db.withTransaction(Propagation.MANDATORY, new TransactionCallback<Isolation>() {
            @Override
            public Isolation execute(@NotNull TransactionContext tx) throws SQLException {
                return Isolation.forJdbcCode(tx.getConnection().getTransactionIsolation());
            }
        });
    }

    @Before
    public void init() {
        Injector injector = Guice.createInjector(new DriverManagerDatabaseModule(), new MyServiceModule(), TestDatabaseProvider.propertiesModule());
        injector.injectMembers(this);
    }

    private static class MyServiceModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(MyService.class).annotatedWith(named("myService1")).to(MyServiceImplementation.class);
            bind(MyService.class).annotatedWith(named("myService2")).to(MyServiceImplementation2.class);
        }
    }
}
