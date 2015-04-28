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

package org.dalesbred.dialects;

import org.dalesbred.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Rule;
import org.junit.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class HsqldbDialectTest {

    private final Database db = Database.forUrlAndCredentials("jdbc:hsqldb:.", "sa", "");

    @Rule
    public final TransactionalTestsRule rule = new TransactionalTestsRule(db);

    @Test
    public void simpleQuery() {
        assertThat(db.findUniqueInt("VALUES (42)"), is(42));
    }

    @Test
    public void detectDialect() {
        db.withTransaction(new TransactionCallback<Void>() {
            @Override
            @Nullable
            public Void execute(@NotNull TransactionContext tx) throws SQLException {
                Dialect dialect = Dialect.detect(tx.getConnection());

                assertThat(dialect, is(instanceOf(HsqldbDialect.class)));
                return null;
            }
        });
    }

    @Test
    public void enumsAsPrimitives() {
        assertThat(db.findUnique(Mood.class, "values ('SAD')"), is(Mood.SAD));
        assertThat(db.findUnique(Mood.class, "values (cast(null as varchar(20)))"), is(nullValue()));
    }

    @Test
    public void enumsAsConstructorParameters() {
        db.update("drop table if exists movie");
        db.update("create temporary table movie (name varchar(64) primary key, mood varchar(20) not null)");

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
