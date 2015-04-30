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

package org.dalesbred;

import org.dalesbred.annotation.Reflective;
import org.dalesbred.dialect.HsqldbDialect;
import org.dalesbred.result.ResultSetProcessor;
import org.dalesbred.result.RowMapper;
import org.dalesbred.transaction.Isolation;
import org.dalesbred.transaction.Propagation;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

public class DatabaseTest {

    private final Database db = TestDatabaseProvider.createInMemoryHSQLDatabase();

    @Rule
    public final TransactionalTestsRule rule = new TransactionalTestsRule(db);

    @Test
    public void meaningfulToString() {
        db.setDefaultIsolation(Isolation.READ_UNCOMMITTED);
        db.setAllowImplicitTransactions(true);
        assertEquals("Database [dialect=" + new HsqldbDialect().toString() + ", allowImplicitTransactions=true, defaultIsolation=READ_UNCOMMITTED, defaultPropagation=DEFAULT]", db.toString());
    }

    @Test
    public void primitivesQueries() {
        assertThat(db.findUniqueInt("values (42)"), is(42));
        assertThat(db.findUnique(Integer.class, "values (42)"), is(42));
        assertThat(db.findUnique(Long.class, "values (cast(42 as bigint))"), is(42L));
        assertThat(db.findUnique(Float.class, "values (42.0)"), is(42.0f));
        assertThat(db.findUnique(Double.class, "values (42.0)"), is(42.0));
        assertThat(db.findUnique(String.class, "values ('foo')"), is("foo"));
        assertThat(db.findUnique(Boolean.class, "values (true)"), is(true));
        assertThat(db.findUnique(Boolean.class, "values (cast(null as boolean))"), is(nullValue()));
    }

    @Test
    public void bigNumbers() {
        assertThat(db.findUnique(BigDecimal.class, "values (4242242848428484848484848)"), is(new BigDecimal("4242242848428484848484848")));
    }

    @Test
    public void autoDetectingTypes() {
        assertThat(db.findUnique(Object.class, "values (42)"), is((Object) 42));
        assertThat(db.findUnique(Object.class, "values ('foo')"), is((Object) "foo"));
        assertThat(db.findUnique(Object.class, "values (true)"), is((Object) true));
    }

    @Test
    public void constructorRowMapping() {
        List<Department> departments = db.findAll(Department.class, "select * from (values (1, 'foo'), (2, 'bar')) d");

        assertThat(departments.size(), is(2));
        assertThat(departments.get(0).id, is(1));
        assertThat(departments.get(0).name, is("foo"));
        assertThat(departments.get(1).id, is(2));
        assertThat(departments.get(1).name, is("bar"));
    }

    @Test
    public void map() {
        Map<Integer, String> map = db.findMap(Integer.class, String.class, "select * from (values (1, 'foo'), (2, 'bar')) d");

        assertThat(map.size(), is(2));
        assertThat(map.get(1), is("foo"));
        assertThat(map.get(2), is("bar"));
    }

    @Test
    public void mapWithNullCoercion() {
        Map<String, String> map = db.findMap(String.class, String.class, "values ('foo', cast (null as clob)), (cast (null as clob), 'bar')");

        assertThat(map.size(), is(2));
        assertThat(map.get("foo"), is(nullValue()));
        assertThat(map.get(null), is("bar"));
    }

    @Test
    public void findUnique_singleResult() {
        assertThat(db.findUnique(Integer.class, "values (42)"), is(42));
    }

    @Test
    public void findUnique_nonUniqueResult() {
        try {
            db.findUnique(Integer.class, "VALUES (1), (2)");
            fail("Expected NonUniqueResultException");
        } catch (NonUniqueResultException e) {
            assertThat(e.getCount(), is(2));
        }
    }

    @Test(expected = NonUniqueResultException.class)
    public void findUnique_emptyResult() {
        db.findUnique(Integer.class, "select * from (values (1)) n where false");
    }

    @Test
    public void findUniqueOrNull_singleResult() {
        assertThat(db.findUniqueOrNull(Integer.class, "values (42)"), is(42));
    }

    @Test(expected = NonUniqueResultException.class)
    public void findUniqueOrNull_nonUniqueResult() {
        db.findUniqueOrNull(Integer.class, "values (1), (2)");
    }

    @Test
    public void findUniqueOrNull_emptyResult() {
        assertThat(db.findUniqueOrNull(Integer.class, "select * from (values (1)) n where false"), is(nullValue()));
    }

    @Test
    public void findOptional_emptyResult() {
        assertThat(db.findOptional(Integer.class, "select * from (values (1)) n where false"), is(Optional.empty()));
    }

    @Test(expected = NonUniqueResultException.class)
    public void findOptional_nonUniqueResult() {
        db.findOptional(Integer.class, "values (1), (2)");
    }

    @Test
    public void rowMapper() {
        RowMapper<Integer> squaringRowMapper = resultSet -> {
            int value = resultSet.getInt(1);
            return value*value;
        };

        assertThat(db.findAll(squaringRowMapper, "values (1), (2), (3)"), is(asList(1, 4, 9)));
        assertThat(db.findUnique(squaringRowMapper, "values (7)"), is(49));
        assertThat(db.findUniqueOrNull(squaringRowMapper, "select * from (values (1)) n where false"), is(nullValue()));
        assertThat(db.findOptional(squaringRowMapper, "values (7)"), is(Optional.of(49)));
        assertThat(db.findOptional(squaringRowMapper, "select * from (values (1)) n where false"), is(Optional.empty()));
    }

    @Test
    public void customResultProcessor() {
        ResultSetProcessor<Integer> rowCounter = resultSet -> {
            int rows = 0;
            while (resultSet.next()) rows++;
            return rows;
        };

        assertThat(db.executeQuery(rowCounter, "values (1), (2), (3)"), is(3));
    }

    @Test(expected = DatabaseException.class)
    public void creatingDatabaseWithJndiDataSourceThrowsExceptionWhenContextIsNotConfigured() {
        Database.forJndiDataSource("foo");
    }

    @Test
    public void isolation() {
        assertSame(Isolation.DEFAULT, db.getDefaultIsolation());

        db.setDefaultIsolation(Isolation.REPEATABLE_READ);
        assertSame(Isolation.REPEATABLE_READ, db.getDefaultIsolation());
    }

    @Test
    public void propagation() {
        assertSame(Propagation.DEFAULT, db.getDefaultPropagation());

        db.setDefaultPropagation(Propagation.NESTED);
        assertSame(Propagation.NESTED, db.getDefaultPropagation());
    }

    @Test
    public void implicitTransactions() {
        db.setAllowImplicitTransactions(false);
        assertFalse(db.isAllowImplicitTransactions());

        db.setAllowImplicitTransactions(true);
        assertTrue(db.isAllowImplicitTransactions());
    }

    @Test
    public void instantiationUsingCustomInstantiator() {
        db.getInstantiatorRegistry().registerInstantiator(Integer.class, arguments ->
                arguments.getValues().get(0).toString().length());

        assertThat(db.findUnique(Integer.class, "values ('foobar')"), is(6));
    }

    public static class Department {
        final int id;
        final String name;

        @Reflective
        public Department(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
