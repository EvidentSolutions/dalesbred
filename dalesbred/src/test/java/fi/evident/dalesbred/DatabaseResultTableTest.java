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

package fi.evident.dalesbred;

import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;

import java.sql.Types;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class DatabaseResultTableTest {

    private final Database db = TestDatabaseProvider.createTestDatabase();

    @Rule
    public final TransactionalTestsRule rule = new TransactionalTestsRule(db);

    @Test
    public void fetchSimpleResultTable() {
        ResultTable table = db.findTable("select 42 as num, 'foo' as str, true as bool");

        assertThat(table.getColumnCount(), is(3));
        assertThat(table.getColumnNames(), is(asList("num", "str", "bool")));
        assertThat(table.getColumnTypes(), is(types(Integer.class, String.class, Boolean.class)));
        assertThat(table.getColumns().toString(), is("[num: java.lang.Integer, str: java.lang.String, bool: java.lang.Boolean]"));
        assertThat(table.getColumns().get(1).getIndex(), is(1));

        assertThat("jdbcType[0]", table.getColumns().get(0).getJdbcType(), is(Types.INTEGER));
        assertThat("databaseType[0]", table.getColumns().get(0).getDatabaseType(), is("int4"));

        assertThat(table.getRowCount(), is(1));
        assertThat(table.getRows().get(0).asList(), is(values(42, "foo", true)));
        assertEquals("foo", table.get(0, 1));
        assertEquals("foo", table.get(0, "str"));

        assertThat(table.toString(), is("ResultTable [columns=[num: java.lang.Integer, str: java.lang.String, bool: java.lang.Boolean], rows=1]"));
    }

    @NotNull
    private static List<Object> values(Object... values) {
        return asList(values);
    }

    @NotNull
    private static List<Class<?>> types(Class<?>... classes) {
        return asList(classes);
    }
}
