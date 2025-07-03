/*
 * Copyright (c) 2024 Evident Solutions Oy
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

package org.dalesbred.dialect;

import org.jetbrains.annotations.NotNull;

/**
 * Support for MariaDB.
 */
public class MariaDBDialect extends Dialect {

    public MariaDBDialect(@NotNull String driverVersion) {
        // MariaDB Connector/J 3.5.2+ reports java.sql.Blob as the class name for binary blobs but returns them directly as byte arrays when fetched,
        // which fools the type conversion system and then causes issues in instantiator lookup.
        // https://jira.mariadb.org/browse/CONJ-1228
        // https://github.com/mariadb-corporation/mariadb-connector-j/commit/39ee017e2cdf37f4a54112cc7765e575d36c6fe6
        if (Version.withNumber(driverVersion).isGreaterThanOrEqualTo(Version.withNumber("3.5.2")))
            resultSetMetaDataTypeOverrides.put("java.sql.Blob", byte[].class);
    }

}
