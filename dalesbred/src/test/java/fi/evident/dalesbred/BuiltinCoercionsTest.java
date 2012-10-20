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

import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BuiltinCoercionsTest {

    private final Database db = TestDatabaseProvider.createTestDatabase();

    @Rule
    public final TransactionalTestsRule rule = new TransactionalTestsRule(db);


    @Test
    public void urlsAndUris() throws Exception {
        db.update("drop table if exists url_and_uri");
        db.update("create table url_and_uri (url varchar(64), uri varchar(64))");

        URL url = new URL("http://example.org");
        URI uri = new URI("http://example.net");

        db.update("insert into url_and_uri (url, uri) values (?, ?)", url, uri);

        UrlAndUri result = db.findUnique(UrlAndUri.class, "select url, uri from url_and_uri");

        assertThat(result.url, is(url));
        assertThat(result.uri, is(uri));
    }

    @Test
    public void shortCoercions() {
        assertThat(db.findUnique(short.class, "select 42"), is((short) 42));
        assertThat(db.findUnique(Short.class, "select 42"), is((short) 42));
        assertThat(db.findUnique(Short.class, "select 42::int8"), is((short) 42));
    }

    @Test
    public void intCoercions() {
        assertThat(db.findUnique(int.class,    "select 42"), is(42));
        assertThat(db.findUnique(Integer.class,"select 42"), is(42));
        assertThat(db.findUnique(Integer.class,"select 42::int8"), is(42));
    }

    @Test
    public void longCoercions() {
        assertThat(db.findUnique(long.class, "select 42"), is(42L));
        assertThat(db.findUnique(Long.class, "select 42"), is(42L));
        assertThat(db.findUniqueLong("select 42"), is(42L));
    }

    @Test
    public void floatCoercions() {
        assertThat(db.findUnique(float.class, "select 42"), is(42f));
        assertThat(db.findUnique(Float.class, "select 42"), is(42f));
    }

    @Test
    public void doubleCoercions() {
        assertThat(db.findUnique(double.class, "select 42"), is(42.0));
        assertThat(db.findUnique(Double.class, "select 42"), is(42.0));
    }

    @Test
    public void bigIntegerCoercions() {
        assertThat(db.findUnique(BigInteger.class, "select 42"), is(BigInteger.valueOf(42)));
    }

    @Test
    public void bigDecimalCoercions() {
        assertThat(db.findUnique(BigDecimal.class, "select 42"), is(BigDecimal.valueOf(42)));
        assertThat(db.findUnique(BigDecimal.class, "select 42"), is(BigDecimal.valueOf(42)));
    }

    @Test
    public void numberCoercions() {
        db.update("drop table if exists numbers");
        db.update("create table numbers (short int2, int int4, long int8, float float4, double float8, bigint numeric, bigdecimal numeric)");

        short shortValue = Short.MAX_VALUE;
        int intValue = Integer.MAX_VALUE;
        long longValue = Long.MAX_VALUE;
        float floatValue = 442.42042f;
        double doubleValue = 42422341233.2424;
        BigInteger bigIntegerValue = new BigInteger("2334593458934593485734985734958734958375984357349857943857");
        BigDecimal bigDecimalValue = new BigDecimal("234239472938472394823.23948723948723948723498237429387423948");

        db.update("insert into numbers (short, int, long, float, double, bigint, bigdecimal) values (?, ?, ?, ?, ?, ?, ?)",
                  shortValue, intValue, longValue, floatValue, doubleValue, bigIntegerValue, bigDecimalValue);

        Numbers numbers = db.findUnique(Numbers.class, "select * from numbers");

        assertThat(numbers.shortValue, is(shortValue));
        assertThat(numbers.intValue, is(intValue));
        assertThat(numbers.longValue, is(longValue));
        assertThat(numbers.floatValue, is(floatValue));
        assertThat(numbers.doubleValue, is(doubleValue));
        assertThat(numbers.bigIntegerValue, is(bigIntegerValue));
        assertThat(numbers.bigDecimalValue, is(bigDecimalValue));
    }

    @Test
    public void count() {
        assertThat(db.findUniqueInt("select count(*) from generate_series(1, 10) n"), is(10));
    }

    public static final class UrlAndUri {
        final URL url;
        final URI uri;

        @Reflective
        public UrlAndUri(URL url, URI uri) {
            this.url = url;
            this.uri = uri;
        }
    }


    public static class Numbers {
        final short shortValue;
        final int intValue;
        final long longValue;
        final float floatValue;
        final double doubleValue;
        final BigInteger bigIntegerValue;
        final BigDecimal bigDecimalValue;

        @Reflective
        public Numbers(short shortValue, int intValue, long longValue, float floatValue, double doubleValue, BigInteger bigIntegerValue, BigDecimal bigDecimalValue) {
            this.shortValue = shortValue;
            this.intValue = intValue;
            this.longValue = longValue;
            this.floatValue = floatValue;
            this.doubleValue = doubleValue;
            this.bigIntegerValue = bigIntegerValue;
            this.bigDecimalValue = bigDecimalValue;
        }
    }
}
