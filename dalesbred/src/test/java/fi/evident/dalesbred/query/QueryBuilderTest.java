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

package fi.evident.dalesbred.query;

import fi.evident.dalesbred.SqlQuery;
import org.junit.Test;

import static fi.evident.dalesbred.SqlQuery.query;
import static org.junit.Assert.assertEquals;

public class QueryBuilderTest {

    @Test
    public void buildingSimpleQuery() {
        QueryBuilder qb = new QueryBuilder("select 42");

        assertEquals(query("select 42"), qb.build());
    }

    @Test
    public void buildingSimpleQueryWithArguments() {
        QueryBuilder qb = new QueryBuilder("select ?, ?", 42, "foo");

        assertEquals(query("select ?, ?", 42, "foo"), qb.build());
    }

    @Test
    public void buildingQueryDynamically() {
        QueryBuilder qb = new QueryBuilder("select * from document");
        qb.append(" where id=?", 4).append(" or status=?", "rejected");

        assertEquals(query("select * from document where id=? or status=?", 4, "rejected"), qb.build());
    }

    @Test
    public void placeholders() {
        QueryBuilder qb = new QueryBuilder("select * from document");
        qb.append(" where id in (").appendPlaceholders(4).append(")").addArguments(1,2,3).addArgument(4);

        assertEquals(query("select * from document where id in (?,?,?,?)", 1, 2, 3, 4), qb.build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroPlaceholders() {
        new QueryBuilder("select * from foo where id in (").appendPlaceholders(0).append(")");
    }

    @Test(expected = IllegalStateException.class)
    public void buildingEmptyQueryThrowsException() {
        new QueryBuilder().build();
    }

    @Test(expected = IllegalStateException.class)
    public void buildingEmptyQueryThrowsException2() {
        new QueryBuilder("").build();
    }

    @Test
    public void placeholder() {
        assertEquals(QueryBuilder.PLACEHOLDER, new QueryBuilder().appendPlaceholders(1).build().getSql());
    }

    @Test
    public void confidentialArguments() {
        SqlQuery query = new QueryBuilder("select * from user where password=?").addConfidentialArgument("foo").build();
        assertEquals("select * from user where password=? [****]", query.toString());
    }
}
