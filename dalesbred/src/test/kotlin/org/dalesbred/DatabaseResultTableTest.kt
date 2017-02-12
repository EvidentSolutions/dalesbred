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
import java.lang.reflect.Type
import java.sql.Types
import kotlin.test.assertEquals

class DatabaseResultTableTest {

    private val db = TestDatabaseProvider.createInMemoryHSQLDatabase()

    @get:Rule val rule = TransactionalTestsRule(db)

    @Test
    fun fetchSimpleResultTable() {
        val table = db.findTable("select 42 as num, 'foo' as str, true as bool from (values (0)) v")

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

    private fun values(vararg values: Any): List<Any> = listOf(*values)

    private fun types(vararg types: Type): List<Type> = listOf(*types)
}
