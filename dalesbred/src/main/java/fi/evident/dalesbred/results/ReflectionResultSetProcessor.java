package fi.evident.dalesbred.results;

import fi.evident.dalesbred.DatabaseException;
import fi.evident.dalesbred.instantiation.Coercions;
import fi.evident.dalesbred.instantiation.Instantiator;
import fi.evident.dalesbred.instantiation.InstantiatorRegistry;
import fi.evident.dalesbred.instantiation.NamedTypeList;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

/**
 * Builds a list of results from {@link ResultSet} using reflection to instantiate individual rows.
 */
public final class ReflectionResultSetProcessor<T> implements ResultSetProcessor<List<T>> {

    private final Class<T> cl;
    private final Coercions coercions;
    private static final InstantiatorRegistry instantiatorRegistry = new InstantiatorRegistry();

    private ReflectionResultSetProcessor(@NotNull Class<T> cl, @NotNull Coercions coercions) {
        this.cl = requireNonNull(cl);
        this.coercions = requireNonNull(coercions);
    }
    
    public static <T> ReflectionResultSetProcessor<T> forClass(@NotNull Class<T> cl, @NotNull Coercions coercions) {
        return new ReflectionResultSetProcessor<T>(cl, coercions);
    }

    @Override
    public List<T> process(ResultSet resultSet) throws SQLException {
        Instantiator<T> ctor = findInstantiator(resultSet.getMetaData());

        ArrayList<T> result = new ArrayList<T>();

        Object[] args = new Object[resultSet.getMetaData().getColumnCount()];

        while (resultSet.next()) {
            for (int i = 0; i < args.length; i++)
                args[i] = resultSet.getObject(i+1);

            result.add(ctor.instantiate(args, coercions));
        }

        return result;
    }

    private Instantiator<T> findInstantiator(ResultSetMetaData metaData) throws SQLException {
        try {
            NamedTypeList types = getTypes(metaData);
            return instantiatorRegistry.findInstantiator(cl, types);
        } catch (NoSuchMethodException e) {
            throw new DatabaseException(e);
        }
    }

    private static NamedTypeList getTypes(ResultSetMetaData metaData) throws SQLException {
        int columns = metaData.getColumnCount();

        NamedTypeList.Builder result = NamedTypeList.builder(columns);

        for (int i = 0; i < columns; i++)
            result.add(metaData.getColumnName(i+1), getColumnType(metaData, i+1));

        return result.build();
    }

    private static Class<?> getColumnType(ResultSetMetaData metaData, int column) throws SQLException {
        String className = metaData.getColumnClassName(column);
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("Could not find class '" + className + "'", e);
        }
    }
}
