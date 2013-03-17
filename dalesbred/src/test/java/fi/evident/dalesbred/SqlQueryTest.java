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

import org.junit.Test;

import java.util.Collections;

import static fi.evident.dalesbred.SqlQuery.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class SqlQueryTest {

    @Test
    public void toStringProvidesMeaningfulInformation() {
        assertThat(query("select bar from foo where id=?", 42, null).toString(),
                   is("select bar from foo where id=? [42, null]"));
    }

    @Test
    public void queriesHaveStructuralEquality() {
        assertThat(query("select * from foo"), is(query("select * from foo")));
        assertThat(query("select * from foo", 1, 2), is(query("select * from foo", 1, 2)));

        assertThat(query("select * from foo"), is(not(query("select * from bar"))));
        assertThat(query("select * from foo", 1, 2), is(not(query("select * from foo", 1, 3))));
    }

    @Test
    public void hashCodeObeysEquality() {
        assertThat(query("select * from foo").hashCode(), is(query("select * from foo").hashCode()));
        assertThat(query("select * from foo", 1, 2).hashCode(), is(query("select * from foo", 1, 2).hashCode()));
    }

    @Test
    public void accessors() {
        SqlQuery query = query("select * from foo", asList(1, 2, 3));

        assertEquals("select * from foo", query.getSql());
        assertEquals(asList(1, 2, 3), query.getArguments());
    }

    @Test
    public void secretValuesAreNotShownInToString() {
        SqlQuery query = query("select * from foo where login=? and password=?", "foo", confidential("bar"));

        assertEquals("select * from foo where login=? and password=? [foo, ****]", query.toString());
    }

    @Test
    public void namedQueryFromMap() {
        SqlQuery query = namedQuery("select * from foo where name = :name", Collections.singletonMap("name", "bar"));

        assertEquals("select * from foo where name = ?", query.getSql());
        assertEquals(singletonList("bar"), query.getArguments());
    }

    @Test
    public void namedQueryFromBean() {
        SqlQuery query = namedQuery("select * from foo where name = :name", new ExampleNamed("bar"));

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
