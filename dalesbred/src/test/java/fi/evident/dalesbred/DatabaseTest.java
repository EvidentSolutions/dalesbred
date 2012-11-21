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

import fi.evident.dalesbred.dialects.PostgreSQLDialect;
import fi.evident.dalesbred.instantiation.InstantiationListener;
import fi.evident.dalesbred.results.ResultSetProcessor;
import fi.evident.dalesbred.results.RowMapper;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static fi.evident.dalesbred.SqlQuery.confidential;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

public class DatabaseTest {

    private final Database db = TestDatabaseProvider.createTestDatabase();

    @Rule
    public final TransactionalTestsRule rule = new TransactionalTestsRule(db);

    @Test
    public void meaningfulToString() {
        db.setDefaultIsolation(Isolation.READ_UNCOMMITTED);
        db.setAllowImplicitTransactions(true);
        assertEquals("Database [dialect=" + new PostgreSQLDialect().toString() + ", allowImplicitTransactions=true, defaultIsolation=READ_UNCOMMITTED, defaultPropagation=DEFAULT]", db.toString());
    }

    @Test
    public void primitivesQueries() {
        assertThat(db.findUniqueInt("select 42"), is(42));
        assertThat(db.findUnique(Integer.class, "select 42"), is(42));
        assertThat(db.findUnique(Long.class, "select 42::int8"), is(42L));
        assertThat(db.findUnique(Float.class, "select 42.0::float4"), is(42.0f));
        assertThat(db.findUnique(Double.class, "select 42.0::float8"), is(42.0));
        assertThat(db.findUnique(String.class, "select 'foo'"), is("foo"));
        assertThat(db.findUnique(Boolean.class, "select true"), is(true));
        assertThat(db.findUnique(Boolean.class, "select null::boolean"), is(nullValue()));
    }

    @Test
    public void bigNumbers() {
        assertThat(db.findUnique(BigDecimal.class, "select 4242242848428484848484848"), is(new BigDecimal("4242242848428484848484848")));
    }

    @Test
    public void autoDetectingTypes() {
        assertThat(db.findUnique(Object.class, "select 42"), is((Object) 42));
        assertThat(db.findUnique(Object.class, "select 'foo'"), is((Object) "foo"));
        assertThat(db.findUnique(Object.class, "select true"), is((Object) true));
    }

    @Test
    public void constructorRowMapping() {
        db.update("drop table if exists department");

        db.update("create table department (id serial primary key, name varchar(64) not null)");
        int id1 = db.findUniqueInt("insert into department (name) values ('foo') returning id");
        int id2 = db.findUniqueInt("insert into department (name) values ('bar') returning id");

        List<Department> departments = db.findAll(Department.class, "select id, name from department");

        assertThat(departments.size(), is(2));
        assertThat(departments.get(0).id, is(id1));
        assertThat(departments.get(0).name, is("foo"));
        assertThat(departments.get(1).id, is(id2));
        assertThat(departments.get(1).name, is("bar"));
    }

    @Test
    public void map() {
        db.update("drop table if exists department");

        db.update("create table department (id serial primary key, name varchar(64) not null)");
        int id1 = db.findUnique(Integer.class, "insert into department (name) values ('foo') returning id");
        int id2 = db.findUnique(Integer.class, "insert into department (name) values ('bar') returning id");

        Map<Integer, String> map = db.findMap(Integer.class, String.class, "select id, name from department");

        assertThat(map.size(), is(2));
        assertThat(map.get(id1), is("foo"));
        assertThat(map.get(id2), is("bar"));
    }

    @Test
    public void enumsAsPrimitives() {
        db.update("drop type if exists mood cascade");
        db.update("create type mood as enum ('SAD', 'HAPPY')");

        db.findUnique(Mood.class, "select 'SAD'::mood").getClass();
        assertThat(db.findUnique(Mood.class, "select 'SAD'::mood"), is(Mood.SAD));
        assertThat(db.findUnique(Mood.class, "select null::mood"), is(nullValue()));
    }

    @Test
    public void enumsAsConstructorParameters() {
        db.update("drop type if exists mood cascade");
        db.update("create type mood as enum ('SAD', 'HAPPY')");

        db.update("drop table if exists movie");
        db.update("create table movie (name varchar(64) primary key, mood mood not null)");

        db.update("insert into movie (name, mood) values (?, ?)", "Amélie", Mood.HAPPY);

        Movie movie = db.findUnique(Movie.class, "select name, mood from movie");
        assertThat(movie.name, is("Amélie"));
        assertThat(movie.mood, is(Mood.HAPPY));
    }

    @Test
    public void findUnique_singleResult() {
        assertThat(db.findUnique(Integer.class, "select 42"), is(42));
    }

    @Test(expected = NonUniqueResultException.class)
    public void findUnique_nonUniqueResult() {
        db.findUnique(Integer.class, "select generate_series(1, 2)");
    }

    @Test(expected = NonUniqueResultException.class)
    public void findUnique_emptyResult() {
        db.findUnique(Integer.class, "select generate_series(0,-1)");
    }

    @Test
    public void findUniqueOrNull_singleResult() {
        assertThat(db.findUniqueOrNull(Integer.class, "select 42"), is(42));
    }

    @Test(expected = NonUniqueResultException.class)
    public void findUniqueOrNull_nonUniqueResult() {
        db.findUniqueOrNull(Integer.class, "select generate_series(1, 2)");
    }

    @Test
    public void findUniqueOrNull_emptyResult() {
        assertThat(db.findUniqueOrNull(Integer.class, "select generate_series(0,-1)"), is(nullValue()));
    }

    @Test
    public void rowMapper() {
        RowMapper<Integer> squaringRowMapper = new RowMapper<Integer>() {
            @NotNull
            @Override
            public Integer mapRow(@NotNull ResultSet resultSet) throws SQLException {
                int value = resultSet.getInt(1);
                return value*value;
            }
        };

        assertThat(db.findAll(squaringRowMapper, "select generate_series(1, 3)"), is(asList(1, 4, 9)));
        assertThat(db.findUnique(squaringRowMapper, "select 7"), is(49));
        assertThat(db.findUniqueOrNull(squaringRowMapper, "select generate_series(0,-1)"), is(nullValue()));
    }

    @Test
    public void customResultProcessor() {
        ResultSetProcessor<Integer> rowCounter = new ResultSetProcessor<Integer>() {
            @Override
            public Integer process(@NotNull ResultSet resultSet) throws SQLException {
                int rows = 0;
                while (resultSet.next()) rows++;
                return rows;
            }
        };

        assertThat(db.executeQuery(rowCounter, "select generate_series(1, 10)"), is(10));
    }

    @Test(expected = DatabaseException.class)
    public void creatingDatabaseWithJndiDataSourceThrowsExceptionWhenContextIsNotConfigured() {
        Database.forJndiDataSource("foo");
    }

    @Test
    public void confidentialValuesWorkLikeNormals() {
        assertThat(db.findUnique(String.class, "select ?", confidential("foo")), is("foo"));
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
    public void returnValueOfUpdate() {
        db.update("drop table if exists department");

        db.update("create table department (id serial primary key, name varchar(64) not null)");
        db.findUnique(Integer.class, "insert into department (name) values ('foo') returning id");
        db.findUnique(Integer.class, "insert into department (name) values ('bar') returning id");

        assertThat(db.update("update department set name=name || 'suffix'"), is(2));
    }

    @Test
    public void instantiationListener() {
        final List<Department> instantiations = new ArrayList<Department>();
        db.getInstantiatorRegistry().addInstantiationListener(new InstantiationListener() {
            @Override
            public void onInstantiation(@NotNull Object object) {
                if (object instanceof Department)
                    instantiations.add((Department) object);
            }
        });

        db.update("drop table if exists department");

        db.update("create table department (id serial primary key, name varchar(64) not null)");
        db.findUnique(Integer.class, "insert into department (name) values ('foo') returning id");
        db.findUnique(Integer.class, "insert into department (name) values ('bar') returning id");

        List<Department> departments = db.findAll(Department.class, "select id, name from department");

        assertEquals(departments, instantiations);
    }

    enum Mood {
        SAD,
        HAPPY
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

    public static class Movie {
        final String name;
        final Mood mood;

        @Reflective
        public Movie(String name, Mood mood) {
            this.name = name;
            this.mood = mood;
        }
    }
}
