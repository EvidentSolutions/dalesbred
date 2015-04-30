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
import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BuiltinConversionsTest {

    private final Database db = TestDatabaseProvider.createInMemoryHSQLDatabase();

    @Rule
    public final TransactionalTestsRule rule = new TransactionalTestsRule(db);

    @Test
    public void urlsAndUris() throws Exception {
        db.update("drop table if exists url_and_uri");
        db.update("create temporary table url_and_uri (url varchar(64), uri varchar(64))");

        URL url = new URL("http://example.org");
        URI uri = new URI("http://example.net");

        db.update("insert into url_and_uri (url, uri) values (?, ?)", url, uri);

        UrlAndUri result = db.findUnique(UrlAndUri.class, "select url, uri from url_and_uri");

        assertThat(result.url.toString(), is(url.toString()));
        assertThat(result.uri, is(uri));
    }

    @Test
    public void shortConversions() {
        assertThat(db.findUnique(short.class, "values (42)"), is((short) 42));
        assertThat(db.findUnique(Short.class, "values (42)"), is((short) 42));
        assertThat(db.findUnique(Short.class, "values (cast(42 as bigint))"), is((short) 42));
    }

    @Test
    public void intConversions() {
        assertThat(db.findUnique(int.class,    "values (42)"), is(42));
        assertThat(db.findUnique(Integer.class,"values (42)"), is(42));
        assertThat(db.findUnique(Integer.class,"values (cast (42 as bigint))"), is(42));
    }

    @Test
    public void longConversions() {
        assertThat(db.findUnique(long.class, "values (42)"), is(42L));
        assertThat(db.findUnique(Long.class, "values (42)"), is(42L));
        assertThat(db.findUniqueLong("values (42)"), is(42L));
    }

    @Test
    public void floatConversions() {
        assertThat(db.findUnique(float.class, "values (42)"), is(42.0f));
        assertThat(db.findUnique(Float.class, "values (42)"), is(42.0f));
    }

    @Test
    public void doubleConversions() {
        assertThat(db.findUnique(double.class, "values (42)"), is(42.0));
        assertThat(db.findUnique(Double.class, "values (42)"), is(42.0));
    }

    @Test
    public void bigIntegerConversions() {
        assertThat(db.findUnique(BigInteger.class, "values (42)"), is(BigInteger.valueOf(42)));
    }

    @Test
    public void bigDecimalConversions() {
        assertThat(db.findUnique(BigDecimal.class, "values (42)"), is(BigDecimal.valueOf(42)));
        assertThat(db.findUnique(BigDecimal.class, "values (42)"), is(BigDecimal.valueOf(42)));
    }

    @Test
    public void numberConversions() {
        db.update("drop table if exists numbers");
        db.update("create temporary table numbers (short smallint, int int, long bigint, float float, double float, bigint numeric, bigdecimal numeric(100,38))");

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
    public void updateCounts() {
        db.update("drop table if exists update_count_test_table");
        db.update("create temporary table update_count_test_table (id int primary key)");

        assertThat(db.update("insert into update_count_test_table (id) values (1), (2), (3)"), is(3));

        assertThat(db.update("delete from update_count_test_table where id > 1"), is(2));
    }

    @Test
    public void count() {
        assertThat(db.findUniqueInt("select count(*) from (values (1), (2), (3)) n"), is(3));
    }

    @Test
    public void timeZoneConversions() {
        db.update("drop table if exists timezones");
        db.update("create temporary table timezones (zone_id varchar(64))");

        TimeZone helsinkiTimeZone = TimeZone.getTimeZone("Europe/Helsinki");

        db.update("insert into timezones (zone_id) values (?)", helsinkiTimeZone);

        assertThat(db.findUnique(TimeZone.class, "select zone_id from timezones"), is(helsinkiTimeZone));
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
