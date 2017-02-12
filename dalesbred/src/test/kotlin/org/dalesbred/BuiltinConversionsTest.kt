/*
 * Copyright (c) 2017 Evident Solutions Oy
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

package org.dalesbred

import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URI
import java.net.URL
import java.util.*
import kotlin.test.assertEquals

class BuiltinConversionsTest {

    private val db = TestDatabaseProvider.createInMemoryHSQLDatabase()

    @get:Rule val rule = TransactionalTestsRule(db)

    @Test
    fun urlsAndUris() {
        db.update("drop table if exists url_and_uri")
        db.update("create temporary table url_and_uri (url varchar(64), uri varchar(64))")

        val url = URL("http://example.org")
        val uri = URI("http://example.net")

        db.update("insert into url_and_uri (url, uri) values (?, ?)", url, uri)

        val result = db.findUnique(UrlAndUri::class.java, "select url, uri from url_and_uri")

        assertEquals(url.toString(), result.url.toString())
        assertEquals(uri, result.uri)
    }

    @Test
    fun shortConversions() {
        assertEquals(42.toShort(), db.findUnique(Short::class.javaPrimitiveType!!, "values (42)"))
        assertEquals(42.toShort(), db.findUnique(Short::class.java, "values (42)"))
        assertEquals(42.toShort(), db.findUnique(Short::class.java, "values (cast(42 as bigint))"))
    }

    @Test
    fun intConversions() {
        assertEquals(42, db.findUnique(Int::class.javaPrimitiveType!!, "values (42)"))
        assertEquals(42, db.findUniqueInt("values (42)"))
        assertEquals(42, db.findUnique(Int::class.java, "values (42)"))
        assertEquals(42, db.findUnique(Int::class.java, "values (cast (42 as bigint))"))
    }

    @Test
    fun longConversions() {
        assertEquals(42L, db.findUnique(Long::class.javaPrimitiveType!!, "values (42)"))
        assertEquals(42L, db.findUnique(Long::class.java, "values (42)"))
        assertEquals(42L, db.findUniqueLong("values (42)"))
    }

    @Test
    fun booleanConversions() {
        assertEquals(true, db.findUnique(Boolean::class.javaPrimitiveType!!, "values true"))
        assertEquals(false, db.findUnique(Boolean::class.java, "values false"))
        assertEquals(true, db.findUniqueBoolean("values true"))
        assertEquals(false, db.findUniqueBoolean("values false"))
    }

    @Test
    fun floatConversions() {
        assertEquals(42.0f, db.findUnique(Float::class.javaPrimitiveType!!, "values (42)"))
        assertEquals(42.0f, db.findUnique(Float::class.java, "values (42)"))
    }

    @Test
    fun doubleConversions() {
        assertEquals(42.0, db.findUnique(Double::class.javaPrimitiveType!!, "values (42)"))
        assertEquals(42.0, db.findUnique(Double::class.java, "values (42)"))
    }

    @Test
    fun bigIntegerConversions() {
        assertEquals(BigInteger.valueOf(42), db.findUnique(BigInteger::class.java, "values (42)"))
    }

    @Test
    fun bigDecimalConversions() {
        assertEquals(BigDecimal.valueOf(42), db.findUnique(BigDecimal::class.java, "values (42)"))
        assertEquals(BigDecimal.valueOf(42), db.findUnique(BigDecimal::class.java, "values (42)"))
    }

    @Test
    fun numberConversions() {
        db.update("drop table if exists numbers")
        db.update("create temporary table numbers (short smallint, int int, long bigint, float float, double float, bigint numeric, bigdecimal numeric(100,38))")

        val shortValue = Short.MAX_VALUE
        val intValue = Int.MAX_VALUE
        val longValue = Long.MAX_VALUE
        val floatValue = 442.42042f
        val doubleValue = 42422341233.2424
        val bigIntegerValue = BigInteger("2334593458934593485734985734958734958375984357349857943857")
        val bigDecimalValue = BigDecimal("234239472938472394823.23948723948723948723498237429387423948")

        db.update("insert into numbers (short, int, long, float, double, bigint, bigdecimal) values (?, ?, ?, ?, ?, ?, ?)",
                shortValue, intValue, longValue, floatValue, doubleValue, bigIntegerValue, bigDecimalValue)

        val numbers = db.findUnique(Numbers::class.java, "select * from numbers")

        assertEquals(shortValue, numbers.shortValue)
        assertEquals(intValue, numbers.intValue)
        assertEquals(longValue, numbers.longValue)
        assertEquals(floatValue, numbers.floatValue)
        assertEquals(doubleValue, numbers.doubleValue)
        assertEquals(bigIntegerValue, numbers.bigIntegerValue)
        assertEquals(bigDecimalValue, numbers.bigDecimalValue)
    }

    @Test
    fun updateCounts() {
        db.update("drop table if exists update_count_test_table")
        db.update("create temporary table update_count_test_table (id int primary key)")

        assertEquals(3, db.update("insert into update_count_test_table (id) values (1), (2), (3)"))

        assertEquals(2, db.update("delete from update_count_test_table where id > 1"))
    }

    @Test
    fun count() {
        assertEquals(3, db.findUniqueInt("select count(*) from (values (1), (2), (3)) n"))
    }

    @Test
    fun timeZoneConversions() {
        db.update("drop table if exists timezones")
        db.update("create temporary table timezones (zone_id varchar(64))")

        val helsinkiTimeZone = TimeZone.getTimeZone("Europe/Helsinki")

        db.update("insert into timezones (zone_id) values (?)", helsinkiTimeZone)

        assertEquals(helsinkiTimeZone, (db.findUnique(TimeZone::class.java, "select zone_id from timezones"))  )
    }

    class UrlAndUri(val url: URL, val uri: URI)

    class Numbers(val shortValue: Short,
                  val intValue: Int,
                  val longValue: Long,
                  val floatValue: Float,
                  val doubleValue: Double,
                  val bigIntegerValue: BigInteger,
                  val bigDecimalValue: BigDecimal)
}
