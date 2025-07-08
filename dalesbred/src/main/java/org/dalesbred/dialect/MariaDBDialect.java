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

import org.dalesbred.internal.utils.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Support for MariaDB.
 */
public class MariaDBDialect extends Dialect {

    private static final Logger log = LoggerFactory.getLogger(MariaDBDialect.class);

    private final @NotNull Map<String, Type> resultSetMetaDataTypeOverrides = new ConcurrentHashMap<>();

    public MariaDBDialect(@NotNull String driverVersion) {

        // MariaDB Connector/J 3.x encodes byte array types in a way that is incompatible with Class.forName
        resultSetMetaDataTypeOverrides.put("byte[]", byte[].class);

        // MariaDB Connector/J 3.5.2+ reports java.sql.Blob as the class name for binary blobs but returns them directly as byte arrays when fetched,
        // which fools the type conversion system and then causes issues in instantiator lookup.
        // https://jira.mariadb.org/browse/CONJ-1228
        // https://github.com/mariadb-corporation/mariadb-connector-j/commit/39ee017e2cdf37f4a54112cc7765e575d36c6fe6
        try {
            if (Version.parse(driverVersion).compareTo(Version.parse("3.5.2")) >= 0)
                resultSetMetaDataTypeOverrides.put("java.sql.Blob", byte[].class);
        } catch (IllegalArgumentException e) {
            log.error("Could not parse driver version: {}", driverVersion, e);
        }
    }

    @Override
    public @Nullable Type overrideResultSetMetaDataType(@NotNull String className) {
        return resultSetMetaDataTypeOverrides.get(className);
    }
}
