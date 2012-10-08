package fi.evident.dalesbred;

import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static fi.evident.dalesbred.SqlQuery.query;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class DatabaseTest {

    @Rule
    public final TransactionalTestsRule rule = new TransactionalTestsRule("connection.properties");

    private final Database db = rule.db;

    @Test
    public void primitivesQueries() {
        assertThat(db.findUniqueInt(query("select 42")), is(42));
        assertThat(db.findUnique(Integer.class, query("select 42")), is(42));
        assertThat(db.findUnique(String.class, query("select 'foo'")), is("foo"));
        assertThat(db.findUnique(Boolean.class, query("select true")), is(true));
        assertThat(db.findUnique(Boolean.class, query("select null::boolean")), is(nullValue()));
    }

    @Test
    public void autoDetectingTypes() {
        assertThat(db.findUnique(Object.class, query("select 42")), is((Object) 42));
        assertThat(db.findUnique(Object.class, query("select 'foo'")), is((Object) "foo"));
        assertThat(db.findUnique(Object.class, query("select true")), is((Object) true));
    }

    @Test
    public void constructorRowMapping() {
        db.update("drop table if exists department");

        db.update("create table department (id serial primary key, name varchar(64) not null)");
        db.update("insert into department (name) values ('foo')");
        db.update("insert into department (name) values ('bar')");

        List<Department> departments = db.findAll(Department.class, "select id, name from department");

        assertThat(departments.size(), is(2));
        assertThat(departments.get(0).name, is("foo"));
        assertThat(departments.get(1).name, is("bar"));
    }

    @Test
    public void enums() {
        db.update("drop type if exists mood");
        db.update("create type mood as enum ('sad', 'ok', 'happy')");

        assertThat(db.findUnique(Mood.class, "select 'sad'::mood"), is(Mood.SAD));
    }

    public enum Mood {
        SAD, OK, HAPPY
    }

    public static class Department {
        final int id;
        final String name;

        public Department(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
