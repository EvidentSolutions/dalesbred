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

import fi.evident.dalesbred.DatabaseException;
import org.jetbrains.annotations.NotNull;

import javax.inject.Provider;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

/**
 * A provider for connection that delegates the requests to given {@link DataSource}.
 */
public final class DataSourceConnectionProvider implements Provider<Connection> {

    private final DataSource dataSource;

    public DataSourceConnectionProvider(@NotNull DataSource dataSource) {
        this.dataSource = requireNonNull(dataSource);
    }

    @Override
    @NotNull
    public Connection get() {
        try {
            Connection connection = dataSource.getConnection();
            if (connection != null)
                return connection;
            else
                throw new DatabaseException("dataSource returned null connection");
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
