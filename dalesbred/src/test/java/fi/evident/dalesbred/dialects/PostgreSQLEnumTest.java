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

package fi.evident.dalesbred.dialects;

import fi.evident.dalesbred.Database;
import fi.evident.dalesbred.Reflective;
import fi.evident.dalesbred.TestDatabaseProvider;
import fi.evident.dalesbred.TransactionalTestsRule;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class PostgreSQLEnumTest {

    private final Database db = TestDatabaseProvider.createPostgreSQLDatabase();

    @Rule
    public final TransactionalTestsRule rule = new TransactionalTestsRule(db);

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
        db.update("create temporary table movie (name varchar(64) primary key, mood mood not null)");

        db.update("insert into movie (name, mood) values (?, ?)", "Amélie", Mood.HAPPY);

        Movie movie = db.findUnique(Movie.class, "select name, mood from movie");
        assertThat(movie.name, is("Amélie"));
        assertThat(movie.mood, is(Mood.HAPPY));
    }

    enum Mood {
        SAD,
        HAPPY
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
