package org.dalesbred

import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import org.dalesbred.testutils.transactionalTest
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@DatabaseTest(POSTGRESQL)
class DatabaseOptionalTest(private val db: Database) {

    @Test
    fun `empty optional is bound as null`() = transactionalTest(db) {
        assertNull(db.findUnique(Int::class.javaObjectType, "values (cast (? as int))", Optional.empty<Any>()))
        assertNull(db.findUnique(Int::class.javaObjectType, "values (cast (? as int))", OptionalInt.empty()))
        assertNull(db.findUnique(Long::class.javaObjectType, "values (cast (? as int))", OptionalLong.empty()))
        assertNull(db.findUnique(Double::class.javaObjectType, "values (cast (? as float))", OptionalDouble.empty()))
    }

    @Test
    fun `non empty optional is bound as value`() = transactionalTest(db) {
        db.update("drop table if exists optional_test")
        db.update("create table optional_test (val VARCHAR(20))")

        db.update("insert into optional_test (val) values (?)", Optional.of("foo"))

        assertEquals(1, db.findUniqueInt("select count(*) from optional_test where val = 'foo'"))
    }

    @Test
    fun `null are handled as empty`() = transactionalTest(db) {
        val obj = db.findUnique(OptionalContainer::class.java, "select (cast (null as int)) as optionalInt, (cast (null as int)) as optionalInteger, null as optionalString from (values (0))")
        assertEquals(OptionalInt.empty(), obj.optionalInt)
        assertEquals(Optional.empty(), obj.optionalInteger)
        assertEquals(Optional.empty(), obj.optionalString)
    }

    @Test
    fun `non nulls are handled as values`() = transactionalTest(db) {
        val obj = db.findUnique(OptionalContainer::class.java,
                "SELECT 35 AS optionalInt, 53 AS optionalInteger, 'foo' AS optionalString, 4242424 as optionalLong, 424.2 as optionalDouble FROM (VALUES (0))")

        assertEquals(OptionalInt.of(35), obj.optionalInt)
        assertEquals(Optional.of(53), obj.optionalInteger)
        assertEquals(Optional.of("foo"), obj.optionalString)
        assertEquals(OptionalLong.of(4242424), obj.optionalLong)
        assertEquals(OptionalDouble.of(424.2), obj.optionalDouble)
    }

    class OptionalContainer {
        lateinit var optionalInt: OptionalInt
        lateinit var optionalInteger: Optional<Int>
        lateinit var optionalString: Optional<String>
        lateinit var optionalLong: OptionalLong
        lateinit var optionalDouble: OptionalDouble
    }
}
