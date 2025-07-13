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

import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import org.dalesbred.testutils.transactionalTest
import kotlin.test.Test
import kotlin.test.assertEquals

@DatabaseTest(POSTGRESQL)
class DatabaseEnumModeTest(private val db: Database) {

    @Test
    fun `name enum mode`() = transactionalTest(db) {
        db.typeConversionRegistry.registerEnumConversion(MyEnum::class.java) { it.name }

        db.update("drop table if exists enum_mode_test")
        db.update("create temporary table enum_mode_test (name varchar(10), value varchar(10))")

        db.update("insert into enum_mode_test (name, value) values ('foo', ?)", MyEnum.FOO)

        assertEquals(MyEnum.FOO, db.findUnique(MyEnum::class.java, "select value from enum_mode_test where name='foo'"))
    }

    @Test
    fun `ordinal enum mode`() = transactionalTest(db) {
        db.typeConversionRegistry.registerEnumConversion(MyEnum::class.java) { it.ordinal }

        db.update("drop table if exists enum_mode_test")
        db.update("create temporary table enum_mode_test (name varchar(10), value int)")

        db.update("insert into enum_mode_test (name, value) values ('foo', ?)", MyEnum.FOO)

        assertEquals(MyEnum.FOO, db.findUnique(MyEnum::class.java, "select value from enum_mode_test where name='foo'"))
    }

    enum class MyEnum {
        FOO, BAR, BAZ
    }
}
