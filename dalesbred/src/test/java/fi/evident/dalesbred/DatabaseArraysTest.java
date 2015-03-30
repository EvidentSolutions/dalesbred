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

package fi.evident.dalesbred;

import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DatabaseArraysTest {

    private final Database db = TestDatabaseProvider.createPostgreSQLDatabase();

    @Rule
    public final TransactionalTestsRule rule = new TransactionalTestsRule(db);

    @Test
    public void databaseArraysAsPrimitiveArrays() {
        assertThat(db.findUnique(int[].class, "values (cast ('{1,5,3}' as numeric array))"), is(new int[] { 1, 5, 3 }));
        assertThat(db.findUnique(long[].class, "values (cast ('{1,6,3}' as numeric array))"), is(new long[] { 1, 6, 3 }));
        assertThat(db.findUnique(short[].class, "values (cast ('{1,6,3}' as numeric array))"), is(new short[] { 1, 6, 3 }));
    }

    @Test
    public void databaseArraysAsWrapperArrays() {
        assertThat(db.findUnique(Integer[].class, "values (cast ('{1,5,3}' as numeric array))"), is(new Integer[] { 1, 5, 3 }));
        assertThat(db.findUnique(Long[].class, "values (cast ('{1,6,3}' as numeric array))"), is(new Long[] { 1L, 6L, 3L }));
        assertThat(db.findUnique(Short[].class, "values (cast ('{1,6,3}' as numeric array))"), is(new Short[]{1, 6, 3}));
    }

    @Test
    public void databaseArraysForBigNumbers() {
        assertThat(db.findUnique(BigInteger[].class, "values (cast ('{1,5,3}' as numeric array))"), is(new BigInteger[]{BigInteger.valueOf(1), BigInteger.valueOf(5L), BigInteger.valueOf(3L)}));
        assertThat(db.findUnique(BigDecimal[].class, "values (cast ('{1,5,3}' as numeric array))"), is(new BigDecimal[]{BigDecimal.valueOf(1), BigDecimal.valueOf(5L), BigDecimal.valueOf(3L)}));
    }

    @Test
    public void databaseArraysForStrings() {
        assertThat(db.findUnique(String[].class, "values (cast ('{foo,bar,baz}' as varchar array))"), is(new String[] { "foo", "bar", "baz" }));
    }
}
