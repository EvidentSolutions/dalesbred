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

package org.dalesbred.junit;

import org.dalesbred.Database;
import org.dalesbred.dialects.DefaultDialect;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestDatabaseProviderTest {

    @Test
    public void defaultDialect() {
        Database db = TestDatabaseProvider.databaseForProperties("hsqldb-connection.properties");

        assertEquals("foo", db.findUnique(String.class, "values ('foo')"));
    }

    @Test
    public void customDialect() {
        Database db = TestDatabaseProvider.databaseForProperties("hsqldb-connection.properties", new UppercaseDialect());
        db.update("drop table if exists my_table");
        db.update("create table my_table (text varchar(64))");

        db.update("insert into my_table values (?)", "foo");

        assertEquals("FOO", db.findUnique(String.class, "select text from my_table"));
    }

    private static final class UppercaseDialect extends DefaultDialect {
        @NotNull
        @Override
        public Object valueToDatabase(@NotNull Object value) {
            return value.toString().toUpperCase();
        }
    }
}
