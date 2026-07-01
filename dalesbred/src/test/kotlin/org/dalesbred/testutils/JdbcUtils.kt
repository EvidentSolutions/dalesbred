package org.dalesbred.testutils

import org.dalesbred.Database
import org.dalesbred.TransactionSettingsTest
import org.dalesbred.transaction.TransactionContext
import java.sql.Connection
import java.sql.ResultSet
import javax.sql.DataSource

@Suppress("ConvertTryFinallyToUseCall")
inline fun <R> Connection.use(block: (Connection) -> R): R =
    try {
        block(this)
    } finally {
        close()
    }

inline fun <R> DataSource.withConnection(block: (Connection) -> R): R =
    connection.use(block)

inline fun <T> ResultSet.mapRows(func: (ResultSet) -> T): List<T> {
    val result = mutableListOf<T>()
    while (next())
        result.add(func(this))
    return result
}

fun <T> transactionalTest(db: Database, block: (TransactionContext) -> T) {
    db.withVoidTransaction { tx -> block(tx) }
}

