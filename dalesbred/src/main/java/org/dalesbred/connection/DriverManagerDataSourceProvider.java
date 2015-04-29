/*
 * Copyright (c) 2015 Evident Solutions Oy
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

package org.dalesbred.connection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.DriverManager;

/**
 * Creates simple {@link DataSource} that simply fetches the connection from {@link DriverManager} without
 * providing any pooling.
 */
public final class DriverManagerDataSourceProvider {

    private DriverManagerDataSourceProvider() {
    }

    @SuppressWarnings("ObjectEquality")
    @NotNull
    public static DataSource createDataSource(@NotNull String url,
                                              @Nullable String user,
                                              @Nullable String password) {
        // Different versions of JDK have differing amount of methods in DataSource-interface, which
        // makes it hard for us to implement. Therefore we'll use the following hack to implement
        // the interface dynamically:
        return (DataSource) Proxy.newProxyInstance(DriverManagerDataSourceProvider.class.getClassLoader(), new Class<?>[]{DataSource.class}, (proxy, method, args) -> {
            switch (method.getName()) {
                case "getConnection":
                    return DriverManager.getConnection(url, user, password);
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "equals":
                    return proxy == args[0];
                case "toString":
                    return "DataSource for " + url;
                default:
                    throw new UnsupportedOperationException("unsupported operation: " + method);
            }
        });
    }
}
