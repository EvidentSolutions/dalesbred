/*
 * Copyright (c) 2012 Evident Solutions Oy
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

package fi.evident.dalesbred.connection;

import fi.evident.dalesbred.DatabaseSQLException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Provider;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

/**
 * A simple provider for connections that simply fetches the connection from {@link DriverManager} without
 * providing any pooling.
 */
public final class DriverManagerConnectionProvider implements Provider<Connection> {

    private final String url;
    private String user;
    private String password;

    public DriverManagerConnectionProvider(@NotNull String url) {
        this(url, null, null);
    }

    public DriverManagerConnectionProvider(@NotNull String url, @Nullable String user, @Nullable String password) {
        this.url = requireNonNull(url);
        this.user = user;
        this.password = password;
    }

    @Override
    @NotNull
    public Connection get() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DatabaseSQLException(e);
        }
    }

    @NotNull
    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
