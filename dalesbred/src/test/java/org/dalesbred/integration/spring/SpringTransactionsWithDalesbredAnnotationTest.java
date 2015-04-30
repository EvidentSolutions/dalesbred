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

package org.dalesbred.integration.spring;

import org.dalesbred.Database;
import org.dalesbred.TestDatabaseProvider;
import org.dalesbred.transaction.Transactional;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SpringTransactionsWithDalesbredAnnotationTest {

    @Test
    public void dalesbredAnnotationIsRecognized() {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SimpleConfiguration.class);
        MyService myService = ctx.getBean(MyService.class);

        assertThat(myService.hasActiveTransaction(), is(true));
    }

    public static class MyService {

        @SuppressWarnings("SpringJavaAutowiringInspection")
        @Autowired
        private Database db;

        @Transactional
        public boolean hasActiveTransaction() {
            return db.hasActiveTransaction();
        }
    }

    @Configuration
    @EnableTransactionManagement
    public static class SimpleConfiguration extends DalesbredConfigurationSupport {

        @Bean
        @Role(BeanDefinition.ROLE_SUPPORT)
        public TransactionAttributeSource transactionAttributeSource() {
            return new AnnotationTransactionAttributeSource(new DalesbredSpringAnnotationParser());
        }

        @Bean
        public MyService service() {
            return new MyService();
        }

        @Bean
        public DataSource dataSource() {
            return TestDatabaseProvider.createInMemoryHSQLDataSource();
        }

        @Bean
        public PlatformTransactionManager transactionManager() {
            return new DataSourceTransactionManager(dataSource());
        }
    }
}
