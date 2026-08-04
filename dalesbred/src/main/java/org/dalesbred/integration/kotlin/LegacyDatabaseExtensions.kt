package org.dalesbred.integration.kotlin

import org.dalesbred.Database
import org.dalesbred.query.SqlQuery
import org.intellij.lang.annotations.Language
import java.sql.ResultSet
import java.util.*

// These are kept around for binary compatibility but hidden from source which picks up the new,
// more general extensions instead.

@Deprecated("use DatabaseAccess instead", level = DeprecationLevel.HIDDEN)
inline fun <reified T : Any> Database.findAll(query: SqlQuery): List<T> =
    findAll(T::class.java, query)

@Deprecated("use DatabaseAccess instead", level = DeprecationLevel.HIDDEN)
inline fun <reified T : Any> Database.findAll(@Language("SQL") sql: String, vararg args: Any?): List<T> =
    findAll(SqlQuery.query(sql, *args))

@Deprecated("use DatabaseAccess instead", level = DeprecationLevel.HIDDEN)
inline fun <reified T : Any> Database.findUnique(query: SqlQuery): T =
    findUnique(T::class.java, query)

@Deprecated("use DatabaseAccess instead", level = DeprecationLevel.HIDDEN)
inline fun <reified T : Any> Database.findUnique(@Language("SQL") sql: String, vararg args: Any?): T =
    findUnique(SqlQuery.query(sql, *args))

@Deprecated("use DatabaseAccess instead", level = DeprecationLevel.HIDDEN)
inline fun <reified T : Any> Database.findUniqueOrNull(query: SqlQuery): T? =
    findUniqueOrNull(T::class.java, query)

@Deprecated("use DatabaseAccess instead", level = DeprecationLevel.HIDDEN)
inline fun <reified T : Any> Database.findUniqueOrNull(@Language("SQL") sql: String, vararg args: Any?): T? =
    findUniqueOrNull(SqlQuery.query(sql, *args))

@Deprecated("use DatabaseAccess instead", level = DeprecationLevel.HIDDEN)
inline fun <reified T : Any> Database.findOptional(query: SqlQuery): Optional<T> =
    findOptional(T::class.java, query)

@Deprecated("use DatabaseAccess instead", level = DeprecationLevel.HIDDEN)
inline fun <reified T : Any> Database.findOptional(@Language("SQL") sql: String, vararg args: Any?): Optional<T> =
    findOptional(SqlQuery.query(sql, *args))

@Deprecated("use DatabaseAccess instead", level = DeprecationLevel.HIDDEN)
inline fun <reified K : Any, reified V : Any> Database.findMap(query: SqlQuery): Map<K, V> =
    findMap(K::class.java, V::class.java, query)

@Deprecated("use DatabaseAccess instead", level = DeprecationLevel.HIDDEN)
inline fun <reified K : Any, reified V : Any> Database.findMap(sql: String, vararg args: Any?): Map<K, V> =
    findMap(SqlQuery.query(sql, *args))

@Deprecated("use DatabaseAccess instead", level = DeprecationLevel.HIDDEN)
fun <T : Any> Database.executeQuery(query: SqlQuery, resultSetProcessor: (ResultSet) -> T): T =
    executeQuery({ resultSetProcessor(it) }, query)

@Deprecated("use DatabaseAccess instead", level = DeprecationLevel.HIDDEN)
fun <T : Any> Database.executeQuery(
    @Language("SQL") sql: String,
    vararg args: Any?,
    resultSetProcessor: (ResultSet) -> T
): T =
    executeQuery(SqlQuery.query(sql, *args), resultSetProcessor)

@Deprecated("use DatabaseAccess instead", level = DeprecationLevel.HIDDEN)
fun <T : Any> Database.findAll(query: SqlQuery, rowMapper: (ResultSet) -> T): List<T> =
    findAll({ rowMapper(it) }, query)

@Deprecated("use DatabaseAccess instead", level = DeprecationLevel.HIDDEN)
fun <T : Any> Database.findAll(sql: String, vararg args: Any?, rowMapper: (ResultSet) -> T): List<T> =
    findAll(SqlQuery.query(sql, *args), rowMapper)

@Deprecated("use DatabaseAccess instead", level = DeprecationLevel.HIDDEN)
fun <T : Any> Database.findUnique(query: SqlQuery, rowMapper: (ResultSet) -> T): T =
    findUnique({ rowMapper(it) }, query)

@Deprecated("use DatabaseAccess instead", level = DeprecationLevel.HIDDEN)
fun <T : Any> Database.findUnique(sql: String, vararg args: Any?, rowMapper: (ResultSet) -> T): T =
    findUnique(SqlQuery.query(sql, *args), rowMapper)

@Deprecated("use DatabaseAccess instead", level = DeprecationLevel.HIDDEN)
fun <T : Any> Database.findUniqueOrNull(query: SqlQuery, rowMapper: (ResultSet) -> T): T? =
    findUniqueOrNull({ rowMapper(it) }, query)

@Deprecated("use DatabaseAccess instead", level = DeprecationLevel.HIDDEN)
fun <T : Any> Database.findUniqueOrNull(sql: String, vararg args: Any?, rowMapper: (ResultSet) -> T): T? =
    findUniqueOrNull(SqlQuery.query(sql, *args), rowMapper)

@Deprecated("use DatabaseAccess instead", level = DeprecationLevel.HIDDEN)
fun <T : Any> Database.findOptional(query: SqlQuery, rowMapper: (ResultSet) -> T): Optional<T> =
    findOptional({ rowMapper(it) }, query)

@Deprecated("use DatabaseAccess instead", level = DeprecationLevel.HIDDEN)
fun <T : Any> Database.findOptional(sql: String, vararg args: Any?, rowMapper: (ResultSet) -> T): Optional<T> =
    findOptional(SqlQuery.query(sql, *args), rowMapper)
