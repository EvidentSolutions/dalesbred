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

package org.dalesbred.internal.result;

import org.dalesbred.annotation.Reflective;
import org.dalesbred.dialect.DefaultDialect;
import org.dalesbred.internal.instantiation.InstantiatorProvider;
import org.dalesbred.result.ResultSetProcessor;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InstantiatorRowMapperTest {

    private final InstantiatorProvider instantiatorRegistry = new InstantiatorProvider(new DefaultDialect());

    @Test
    public void instantiatingWithSimpleConstructor() throws SQLException {
        ResultSetProcessor<List<SingleConstructor>> mapper =
                new InstantiatorRowMapper<>(SingleConstructor.class, instantiatorRegistry).list();

        ResultSet resultSet = resultSet(new Object[][] {
            { 1, "foo" },
            { 3, "bar" }
        });

        List<SingleConstructor> list = mapper.process(resultSet);
        assertThat(list.size(), is(2));

        assertThat(list.get(0).num, is(1));
        assertThat(list.get(0).str, is("foo"));
        assertThat(list.get(1).num, is(3));
        assertThat(list.get(1).str, is("bar"));
    }

    @Test
    public void emptyResultSetProducesNoResults() throws SQLException {
        ResultSetProcessor<List<SingleConstructor>> mapper =
                new InstantiatorRowMapper<>(SingleConstructor.class, instantiatorRegistry).list();

        assertThat(mapper.process(emptyResultSet(Integer.class, String.class)).isEmpty(), is(true));
    }

    @Test
    public void correctConstructorIsPickedBasedOnTypes() throws SQLException {
        ResultSetProcessor<List<TwoConstructors>> mapper =
                new InstantiatorRowMapper<>(TwoConstructors.class, instantiatorRegistry).list();

        List<TwoConstructors> list = mapper.process(singletonResultSet(1, "foo"));
        assertThat(list.size(), is(1));

        assertThat(list.get(0).num, is(1));
        assertThat(list.get(0).str, is("foo"));
    }

    public static class SingleConstructor {
        final int num;
        final String str;

        @Reflective
        public SingleConstructor(int num, String str) {
            this.num = num;
            this.str = str;
        }
    }

    public static class TwoConstructors {
        int num;
        String str;

        @Reflective
        public TwoConstructors(int num, String str) {
            this.num = num;
            this.str = str;
        }

        @Reflective
        public TwoConstructors(int num, boolean flag) {
            throw new RuntimeException("unexpected call two wrong constructor");
        }
    }

    private static ResultSet emptyResultSet(@NotNull Class<?>... types) throws SQLException {
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(types.length);

        for (int i = 0; i < types.length; i++) {
            when(metaData.getColumnLabel(i + 1)).thenReturn("column" + i);
            when(metaData.getColumnClassName(i + 1)).thenReturn(types[i].getName());
        }

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getMetaData()).thenReturn(metaData);

        when(resultSet.next()).thenReturn(false);

        return resultSet;
    }

    private static ResultSet singletonResultSet(@NotNull Object... values) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData metadata = metadataFromRow(values);
        when(resultSet.getMetaData()).thenReturn(metadata);

        when(resultSet.next()).thenReturn(true).thenReturn(false);

        OngoingStubbing<Object> getObjectStubbing = when(resultSet.getObject(anyInt()));
        for (Object value : values)
            getObjectStubbing = getObjectStubbing.thenReturn(value);

        return resultSet;
    }

    private static ResultSet resultSet(@NotNull Object[][] rows) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData metadata = metadataFromRow(rows[0]);
        when(resultSet.getMetaData()).thenReturn(metadata);

        OngoingStubbing<Boolean> nextStubbing = when(resultSet.next());
        for (@SuppressWarnings("unused") Object[] row : rows)
            nextStubbing = nextStubbing.thenReturn(true);
        nextStubbing.thenReturn(false);

        OngoingStubbing<Object> getObjectStubbing = when(resultSet.getObject(anyInt()));
        for (Object[] row : rows)
            for (Object col : row)
                getObjectStubbing = getObjectStubbing.thenReturn(col);

        return resultSet;
    }

    private static ResultSetMetaData metadataFromRow(@NotNull Object[] row) throws SQLException {
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(row.length);

        for (int i = 0; i < row.length; i++) {
            when(metaData.getColumnLabel(i + 1)).thenReturn("column" + i);
            when(metaData.getColumnClassName(i + 1)).thenReturn(row[i].getClass().getName());
        }

        return metaData;
    }
}
