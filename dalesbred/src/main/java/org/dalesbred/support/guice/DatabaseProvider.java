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

package org.dalesbred.support.guice;

import com.google.inject.Injector;
import com.google.inject.Provider;
import org.dalesbred.Database;
import org.dalesbred.instantiation.InstantiationListener;
import org.dalesbred.utils.Require;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.sql.DataSource;

final class DatabaseProvider implements Provider<Database> {

    @NotNull
    private final Injector injector;

    @NotNull
    private final DataSource dataSource;

    @Inject
    DatabaseProvider(@NotNull DataSource dataSource, @NotNull Injector injector) {
        this.dataSource = Require.requireNonNull(dataSource);
        this.injector = Require.requireNonNull(injector);
    }

    @Override
    public Database get() {
        Database db = new Database(dataSource);
        db.getInstantiatorRegistry().addInstantiationListener(new InstantiationListener() {
            @Override
            public void onInstantiation(@NotNull Object object) {
                injector.injectMembers(object);
            }
        });
        return db;
    }
}
