package org.dalesbred.integration.kotlin

import org.dalesbred.DatabaseAccess
import org.dalesbred.query.SqlQuery
import org.dalesbred.result.EmptyResultException
import org.dalesbred.result.NonUniqueResultException
import org.intellij.lang.annotations.Language
import java.sql.ResultSet
import java.util.*

/**
 * Executes a query and converts the results to instances of given class using default mechanisms.
 */
inline fun <reified T : Any> DatabaseAccess.findAll(query: SqlQuery): List<T> =
    findAll(T::class.java, query)

/**
 * Executes a query and converts the results to instances of given class using default mechanisms.
 */
inline fun <reified T : Any> DatabaseAccess.findAll(@Language("SQL") sql: String, vararg args: Any?): List<T> =
    findAll(SqlQuery.query(sql, *args))

/**
 * Finds a unique result from database, using given {@link RowMapper} to convert the row.
 *
 * @throws NonUniqueResultException if there is more then one row
 * @throws EmptyResultException if there are no rows
 */
inline fun <reified T : Any> DatabaseAccess.findUnique(query: SqlQuery): T =
    findUnique(T::class.java, query)

/**
 * Finds a unique result from database, using given {@link RowMapper} to convert the row.
 *
 * @throws NonUniqueResultException if there is more then one row
 * @throws EmptyResultException if there are no rows
 */
inline fun <reified T : Any> DatabaseAccess.findUnique(@Language("SQL") sql: String, vararg args: Any?): T =
    findUnique(SqlQuery.query(sql, *args))

inline fun <reified T : Any> DatabaseAccess.findUniqueOrNull(query: SqlQuery): T? =
    findUniqueOrNull(T::class.java, query)

inline fun <reified T : Any> DatabaseAccess.findUniqueOrNull(@Language("SQL") sql: String, vararg args: Any?): T? =
    findUniqueOrNull(SqlQuery.query(sql, *args))

inline fun <reified T : Any> DatabaseAccess.findOptional(query: SqlQuery): Optional<T> =
    findOptional(T::class.java, query)

inline fun <reified T : Any> DatabaseAccess.findOptional(@Language("SQL") sql: String, vararg args: Any?): Optional<T> =
    findOptional(SqlQuery.query(sql, *args))

inline fun <reified K : Any, reified V : Any> DatabaseAccess.findMap(query: SqlQuery): Map<K, V> =
    findMap(K::class.java, V::class.java, query)

inline fun <reified K : Any, reified V : Any> DatabaseAccess.findMap(
    @Language("SQL") sql: String,
    vararg args: Any?
): Map<K, V> =
    findMap(SqlQuery.query(sql, *args))

fun <T : Any> DatabaseAccess.executeQuery(query: SqlQuery, resultSetProcessor: (ResultSet) -> T): T =
    executeQuery({ resultSetProcessor(it) }, query)

fun <T : Any> DatabaseAccess.executeQuery(
    @Language("SQL") sql: String,
    vararg args: Any?,
    resultSetProcessor: (ResultSet) -> T
): T =
    executeQuery(SqlQuery.query(sql, *args), resultSetProcessor)

fun <T : Any> DatabaseAccess.findAll(query: SqlQuery, rowMapper: (ResultSet) -> T): List<T> =
    findAll({ rowMapper(it) }, query)

fun <T : Any> DatabaseAccess.findAll(@Language("SQL") sql: String, vararg args: Any?, rowMapper: (ResultSet) -> T): List<T> =
    findAll(SqlQuery.query(sql, *args), rowMapper)

fun <T : Any> DatabaseAccess.findUnique(query: SqlQuery, rowMapper: (ResultSet) -> T): T =
    findUnique({ rowMapper(it) }, query)

fun <T : Any> DatabaseAccess.findUnique(@Language("SQL") sql: String, vararg args: Any?, rowMapper: (ResultSet) -> T): T =
    findUnique(SqlQuery.query(sql, *args), rowMapper)

fun <T : Any> DatabaseAccess.findUniqueOrNull(query: SqlQuery, rowMapper: (ResultSet) -> T): T? =
    findUniqueOrNull({ rowMapper(it) }, query)

fun <T : Any> DatabaseAccess.findUniqueOrNull(
    @Language("SQL") sql: String,
    vararg args: Any?,
    rowMapper: (ResultSet) -> T
): T? =
    findUniqueOrNull(SqlQuery.query(sql, *args), rowMapper)

fun <T : Any> DatabaseAccess.findOptional(query: SqlQuery, rowMapper: (ResultSet) -> T): Optional<T> =
    findOptional({ rowMapper(it) }, query)

fun <T : Any> DatabaseAccess.findOptional(
    @Language("SQL") sql: String,
    vararg args: Any?,
    rowMapper: (ResultSet) -> T
): Optional<T> =
    findOptional(SqlQuery.query(sql, *args), rowMapper)
