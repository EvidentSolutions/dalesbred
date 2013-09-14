/*
 * Copyright (c) 2013 Evident Solutions Oy
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

package fi.evident.dalesbred;

import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DatabaseEnumModeTest {

    private final Database db = TestDatabaseProvider.createInMemoryHSQLDatabase();

    @Rule
    public final TransactionalTestsRule rule = new TransactionalTestsRule(db);

    @Test
    public void defaultEnumModeIsNative() {
        assertThat(db.getEnumMode(), is(EnumMode.NATIVE));
    }

    @Test
    public void nameEnumMode() {
        db.setEnumMode(EnumMode.NAME);

        db.update("drop table if exists enum_mode_test");
        db.update("create temporary table enum_mode_test (name varchar(10), value varchar(10))");

        db.update("insert into enum_mode_test (name, value) values ('foo', ?)", MyEnum.FOO);

        MyEnum result = db.findUnique(MyEnum.class, "select value from enum_mode_test where name='foo'");
        assertThat(result, is(MyEnum.FOO));
    }

    @Test
    public void ordinalEnumMode() {
        db.setEnumMode(EnumMode.ORDINAL);

        db.update("drop table if exists enum_mode_test");
        db.update("create temporary table enum_mode_test (name varchar(10), value int)");

        db.update("insert into enum_mode_test (name, value) values ('foo', ?)", MyEnum.FOO);

        MyEnum result = db.findUnique(MyEnum.class, "select value from enum_mode_test where name='foo'");
        assertThat(result, is(MyEnum.FOO));
    }

    @SuppressWarnings("UnusedDeclaration")
    public enum MyEnum {
        FOO, BAR, BAZ
    }
}
