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

package org.dalesbred.internal.result

import org.dalesbred.dialect.DefaultDialect
import org.dalesbred.internal.instantiation.InstantiatorProvider
import org.dalesbred.testutils.unimplemented
import org.junit.Test
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import kotlin.test.assertEquals

class InstantiatorRowMapperTest {

    private val instantiatorRegistry = InstantiatorProvider(DefaultDialect())

    @Test
    fun `instantiating with simple constructor`() {
        val mapper = InstantiatorRowMapper(SingleConstructor::class.java, instantiatorRegistry).list()

        val resultSet = resultSet(listOf(listOf(1, "foo"), listOf(3, "bar")))

        val list = mapper.process(resultSet)
        assertEquals(2, list.size)

        assertEquals(1, list[0].num)
        assertEquals("foo", list[0].str)
        assertEquals(3, list[1].num)
        assertEquals("bar", list[1].str)
    }

    @Test
    fun `empty result set produces no results`() {
        val mapper = InstantiatorRowMapper(SingleConstructor::class.java, instantiatorRegistry).list()

        val metadata = metadataFromTypes(arrayOf<Class<*>>(Int::class.java, String::class.java).asList())

        assertEquals(emptyList(), mapper.process(resultSet(emptyList(), metadata)))
    }

    @Test
    fun `correct constructor is picked based on types`() {
        val mapper = InstantiatorRowMapper(TwoConstructors::class.java, instantiatorRegistry).list()

        val list = mapper.process(resultSet(listOf(listOf(1, "foo"))))
        assertEquals(1, list.size)

        assertEquals(1, list[0].num)
        assertEquals("foo", list[0].str)
    }

    class SingleConstructor(val num: Int, val str: String)

    class TwoConstructors(val num: Int, val str: String) {

        @Suppress("UNREACHABLE_CODE", "unused", "UNUSED_PARAMETER")
        constructor(num: Int, flag: Boolean) : this(
            throw RuntimeException("unexpected call two wrong constructor"), ""
        )
    }

    private fun resultSet(rows: List<List<Any>>, metadata: ResultSetMetaData = metadataFromTypes(rows.first().map { it.javaClass })) = object : ResultSet by unimplemented() {
        var index = -1

        override fun getMetaData() = metadata
        override fun next(): Boolean {
            if (index + 1 == rows.size)
                return false

            index++
            return true
        }

        override fun getObject(columnIndex: Int): Any = rows[index][columnIndex - 1]
    }

    private fun metadataFromTypes(types: List<Class<*>>): ResultSetMetaData =
        object : ResultSetMetaData by unimplemented() {
            override fun getColumnCount() = types.size
            override fun getColumnLabel(column: Int) = "column ${column - 1}"
            override fun getColumnClassName(column: Int) = types[column - 1].name
        }
}
