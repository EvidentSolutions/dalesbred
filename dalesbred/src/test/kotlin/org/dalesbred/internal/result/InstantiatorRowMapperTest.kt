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
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.mockito.Mockito.`when` as whenCalled

class InstantiatorRowMapperTest {

    private val instantiatorRegistry = InstantiatorProvider(DefaultDialect())

    @Test
    fun instantiatingWithSimpleConstructor() {
        val mapper = InstantiatorRowMapper(SingleConstructor::class.java, instantiatorRegistry).list()

        val resultSet = resultSet(arrayOf(arrayOf(1, "foo"), arrayOf(3, "bar")))

        val list = mapper.process(resultSet)
        assertEquals(2, list.size)
        
        assertEquals(1, list[0].num)
        assertEquals("foo", list[0].str)
        assertEquals(3, list[1].num)
        assertEquals("bar", list[1].str)
    }

    @Test
    fun emptyResultSetProducesNoResults() {
        val mapper = InstantiatorRowMapper(SingleConstructor::class.java, instantiatorRegistry).list()

        assertTrue(mapper.process(emptyResultSet(Int::class.java, String::class.java)).isEmpty())
    }

    @Test
    fun correctConstructorIsPickedBasedOnTypes() {
        val mapper = InstantiatorRowMapper(TwoConstructors::class.java, instantiatorRegistry).list()

        val list = mapper.process(singletonResultSet(1, "foo"))
        assertEquals(1, list.size)

        assertEquals(1, list[0].num)
        assertEquals("foo", list[0].str)
    }

    class SingleConstructor(val num: Int, val str: String)

    class TwoConstructors(val num: Int, val str: String) {

        @Suppress("UNREACHABLE_CODE", "unused", "UNUSED_PARAMETER")
        constructor(num: Int, flag: Boolean):
            this(throw RuntimeException("unexpected call two wrong constructor"), "")
    }

    private fun emptyResultSet(vararg types: Class<*>): ResultSet {
        val metaData = mock(ResultSetMetaData::class.java)
        whenCalled(metaData.columnCount).thenReturn(types.size)

        for (i in types.indices) {
            whenCalled(metaData.getColumnLabel(i + 1)).thenReturn("column" + i)
            whenCalled(metaData.getColumnClassName(i + 1)).thenReturn(types[i].name)
        }

        val resultSet = mock(ResultSet::class.java)
        whenCalled(resultSet.metaData).thenReturn(metaData)

        whenCalled(resultSet.next()).thenReturn(false)

        return resultSet
    }

    private fun singletonResultSet(vararg values: Any): ResultSet {
        val resultSet = mock(ResultSet::class.java)
        val metadata = metadataFromRow(values)
        whenCalled(resultSet.metaData).thenReturn(metadata)

        whenCalled(resultSet.next()).thenReturn(true).thenReturn(false)

        var getObjectStubbing = whenCalled(resultSet.getObject(ArgumentMatchers.anyInt()))
        for (value in values)
            getObjectStubbing = getObjectStubbing.thenReturn(value)

        return resultSet
    }

    private fun resultSet(rows: Array<Array<Any>>): ResultSet {
        val resultSet = mock(ResultSet::class.java)
        val metadata = metadataFromRow(rows[0])
        whenCalled(resultSet.metaData).thenReturn(metadata)

        var nextStubbing = whenCalled(resultSet.next())
        for (row in rows)
            nextStubbing = nextStubbing.thenReturn(true)
        nextStubbing.thenReturn(false)

        var getObjectStubbing = whenCalled<Any>(resultSet.getObject(ArgumentMatchers.anyInt()))
        for (row in rows)
            for (col in row)
                getObjectStubbing = getObjectStubbing.thenReturn(col)

        return resultSet
    }

    private fun metadataFromRow(row: Array<*>): ResultSetMetaData {
        val metaData = mock(ResultSetMetaData::class.java)
        whenCalled(metaData.columnCount).thenReturn(row.size)

        for (i in row.indices) {
            whenCalled(metaData.getColumnLabel(i + 1)).thenReturn("column" + i)
            whenCalled(metaData.getColumnClassName(i + 1)).thenReturn(row[i]!!.javaClass.name)
        }

        return metaData
    }
}
