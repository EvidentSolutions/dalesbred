package fi.evident.dalesbred;

import fi.evident.dalesbred.dialects.DefaultDialect;
import fi.evident.dalesbred.instantiation.Coercions;
import fi.evident.dalesbred.results.ReflectionResultSetProcessor;
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

public class ReflectionRowMapperTest {

    private final Coercions coercions = new Coercions(new DefaultDialect());

    @Test
    public void instantiatingWithSimpleConstructor() throws SQLException {
        ReflectionResultSetProcessor<SingleConstructor> mapper = ReflectionResultSetProcessor.forClass(SingleConstructor.class, coercions);

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
        ReflectionResultSetProcessor<SingleConstructor> mapper = ReflectionResultSetProcessor.forClass(SingleConstructor.class, coercions);

        assertThat(mapper.process(emptyResultSet(Integer.class, String.class)).isEmpty(), is(true));
    }

    @Test
    public void correctConstructorIsPickedBasedOnTypes() throws SQLException {
        ReflectionResultSetProcessor<TwoConstructors> mapper = ReflectionResultSetProcessor.forClass(TwoConstructors.class, coercions);

        List<TwoConstructors> list = mapper.process(singletonResultSet(1, "foo"));
        assertThat(list.size(), is(1));

        assertThat(list.get(0).num, is(1));
        assertThat(list.get(0).str, is("foo"));
    }

    public static class SingleConstructor {
        int num;
        String str;

        public SingleConstructor(int num, String str) {
            this.num = num;
            this.str = str;
        }
    }

    @SuppressWarnings("unused")
    public static class TwoConstructors {
        int num;
        String str;

        public TwoConstructors(int num, String str) {
            this.num = num;
            this.str = str;
        }

        public TwoConstructors(int foo, boolean str) {
            throw new RuntimeException("unexpected call two wrong constructor");
        }
    }

    private static ResultSet emptyResultSet(Class<?>... types) throws SQLException {
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(types.length);

        for (int i = 0; i < types.length; i++) {
            when(metaData.getColumnName(i + 1)).thenReturn("column" + i);
            when(metaData.getColumnClassName(i + 1)).thenReturn(types[i].getName());
        }

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getMetaData()).thenReturn(metaData);

        when(resultSet.next()).thenReturn(false);

        return resultSet;
    }

    private static ResultSet singletonResultSet(Object... values) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData metadata = metadataFromRow(values);
        when(resultSet.getMetaData()).thenReturn(metadata);

        when(resultSet.next()).thenReturn(true).thenReturn(false);

        OngoingStubbing<Object> getObjectStubbing = when(resultSet.getObject(anyInt()));
        for (Object value : values)
            getObjectStubbing = getObjectStubbing.thenReturn(value);

        return resultSet;
    }

    private static ResultSet resultSet(Object[][] rows) throws SQLException {
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

    private static ResultSetMetaData metadataFromRow(Object[] row) throws SQLException {
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(row.length);

        for (int i = 0; i < row.length; i++) {
            when(metaData.getColumnName(i + 1)).thenReturn("column" + i);
            when(metaData.getColumnClassName(i + 1)).thenReturn(row[i].getClass().getName());
        }

        return metaData;
    }
}
