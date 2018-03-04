/*
 * Copyright (c) 2018 Evident Solutions Oy
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
import java.lang.reflect.Type
import java.sql.Types
import java.time.LocalDate
import kotlin.test.assertEquals

class DatabaseResultTableTest {

    private val db = TestDatabaseProvider.createInMemoryHSQLDatabase()

    @get:Rule
    val rule = TransactionalTestsRule(db)

    @Test
    fun fetchSimpleResultTable() {
        val table = db.findTable("SELECT 42 AS num, 'foo' AS str, TRUE AS bool FROM (VALUES (0)) v")

        assertEquals(3, table.columnCount)
        assertEquals(listOf("NUM", "STR", "BOOL"), table.columnNames)
        assertEquals(types(Int::class.javaObjectType, String::class.java, Boolean::class.javaObjectType), table.columnTypes)
        assertEquals(types(Int::class.javaObjectType, String::class.java, Boolean::class.javaObjectType), table.rawColumnTypes)
        assertEquals("[NUM: java.lang.Integer, STR: java.lang.String, BOOL: java.lang.Boolean]", table.columns.toString())
        assertEquals(1, table.columns[1].index)

        assertEquals(Types.INTEGER, table.columns[0].jdbcType, message = "jdbcType[0]")
        assertEquals("INTEGER", table.columns[0].databaseType, message = "databaseType[0]")

        assertEquals(1, table.rowCount)
        assertEquals(values(42, "foo", true), table.rows[0].asList())
        assertEquals("foo", table.get(0, 1))
        assertEquals("foo", table.get(0, "str"))

        assertEquals("ResultTable [columns=[NUM: java.lang.Integer, STR: java.lang.String, BOOL: java.lang.Boolean], rows=1]", table.toString())
    }

    @Test
    fun testFormatTable() {
        db.update("DROP TABLE IF EXISTS result_table_formatter")
        db.update("CREATE TEMPORARY TABLE result_table_formatter (id INTEGER PRIMARY KEY, created DATE, text VARCHAR(512))")

        db.update("INSERT INTO result_table_formatter (id, created, text) VALUES (?, ?, ?)", 1, LocalDate.of(100, 1, 1), "abc")
        db.update("INSERT INTO result_table_formatter (id, created, text) VALUES (?, ?, ?)", 2, LocalDate.of(2000, 5, 1), "lorem ipsum dolor sit amet")
        db.update("INSERT INTO result_table_formatter (id, created, text) VALUES (?, ?, ?)", 3, LocalDate.of(2018, 3, 3), "Foxes are small-to-medium-sized, omnivorous mammals belonging to several genera of the family Canidae. Foxes have a flattened skull, upright triangular ears, a pointed, slightly upturned snout, and a long bushy tail.")

        val rt = db.findTable("SELECT id, created, text FROM result_table_formatter")

        val expected = """
               | ID | CREATED    | TEXT                                               |
               | -- | ---------- | -------------------------------------------------- |
               | 1  | 0100-01-01 | abc                                                |
               | 2  | 2000-05-01 | lorem ipsum dolor sit amet                         |
               | 3  | 2018-03-03 | Foxes are small-to-medium-sized, omnivorous mam... |

        """.trimIndent()

        assertEquals(expected, rt.toStringFormatted())
        assertEquals(expected, buildString { rt.formatTo(this) })
    }

    private fun values(vararg values: Any): List<Any> = listOf(*values)

    private fun types(vararg types: Type): List<Type> = listOf(*types)
}
