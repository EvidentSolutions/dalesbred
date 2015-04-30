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
import org.dalesbred.query.SqlQuery;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Collections;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class SqlQueryTest {

    @Test
    public void toStringProvidesMeaningfulInformation() {
        assertThat(SqlQuery.query("select bar from foo where id=?", 42, null).toString(),
                   is("select bar from foo where id=? [42, null]"));
    }

    @Test
    public void queriesHaveStructuralEquality() {
        assertThat(SqlQuery.query("select * from foo"), is(SqlQuery.query("select * from foo")));
        assertThat(SqlQuery.query("select * from foo", 1, 2), is(SqlQuery.query("select * from foo", 1, 2)));

        assertThat(SqlQuery.query("select * from foo"), is(CoreMatchers.not(SqlQuery.query("select * from bar"))));
        assertThat(SqlQuery.query("select * from foo", 1, 2), is(CoreMatchers.not(SqlQuery.query("select * from foo", 1, 3))));
    }

    @Test
    public void hashCodeObeysEquality() {
        assertThat(SqlQuery.query("select * from foo").hashCode(), is(SqlQuery.query("select * from foo").hashCode()));
        assertThat(SqlQuery.query("select * from foo", 1, 2).hashCode(), is(SqlQuery.query("select * from foo", 1, 2).hashCode()));
    }

    @Test
    public void accessors() {
        SqlQuery query = SqlQuery.query("select * from foo", asList(1, 2, 3));

        assertEquals("select * from foo", query.getSql());
        assertEquals(asList(1, 2, 3), query.getArguments());
    }

    @Test
    public void namedQueryFromMap() {
        SqlQuery query = SqlQuery.namedQuery("select * from foo where name = :name", Collections.singletonMap("name", "bar"));

        assertEquals("select * from foo where name = ?", query.getSql());
        assertEquals(singletonList("bar"), query.getArguments());
    }

    @Test
    public void namedQueryFromBean() {
        SqlQuery query = SqlQuery.namedQuery("select * from foo where name = :name", new ExampleNamed("bar"));

        assertEquals("select * from foo where name = ?", query.getSql());
        assertEquals(singletonList("bar"), query.getArguments());
    }

    public static final class ExampleNamed {

        @Reflective
        public final String name;

        public ExampleNamed(String name) {
            this.name = name;
        }
    }
}
