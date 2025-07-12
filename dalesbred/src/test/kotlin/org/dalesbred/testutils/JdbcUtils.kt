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

package org.dalesbred.testutils

import org.dalesbred.Database
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

fun <T> transactionalTest(db: Database, block: (Database) -> T) {
    db.withTransaction { block(db) }
}

