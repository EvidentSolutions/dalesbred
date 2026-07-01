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
