package fi.evident.dalesbred;

import fi.evident.dalesbred.instantiation.Instantiator;
import fi.evident.dalesbred.instantiation.InstantiatorRegistry;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

final class ReflectionRowMapper<T> implements JdbcCallback<ResultSet, List<T>> {

    private final Class<T> cl;
    private static final InstantiatorRegistry instantiatorRegistry = new InstantiatorRegistry();

    private ReflectionRowMapper(Class<T> cl) {
        this.cl = requireNonNull(cl);
    }
    
    public static <T> ReflectionRowMapper<T> forClass(Class<T> cl) {
        return new ReflectionRowMapper<T>(cl);
    }

    @Override
    public List<T> execute(ResultSet resultSet) throws SQLException {
        Instantiator<T> ctor = findConstructor(resultSet.getMetaData());

        ArrayList<T> result = new ArrayList<T>();

        Object[] args = new Object[resultSet.getMetaData().getColumnCount()];

        while (resultSet.next()) {
            for (int i = 0; i < args.length; i++)
                args[i] = resultSet.getObject(i+1);

            result.add(ctor.instantiate(args));
        }

        return result;
    }

    private Instantiator<T> findConstructor(ResultSetMetaData metaData) throws SQLException {
        try {
            Class<?>[] types = getTypes(metaData);
            return instantiatorRegistry.findInstantiator(cl, types);
        } catch (NoSuchMethodException e) {
            throw new JdbcException(e);
        }
    }

    private static Class<?>[] getTypes(ResultSetMetaData metaData) throws SQLException {
        Class<?>[] types = new Class<?>[metaData.getColumnCount()];

        for (int i = 0; i < types.length; i++)
            types[i] = getColumnType(metaData, i+1);

        return types;
    }

    private static Class<?> getColumnType(ResultSetMetaData metaData, int column) throws SQLException {
        String className = metaData.getColumnClassName(column);
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new JdbcException("Could not find class '" + className + "'", e);
        }
    }
}
