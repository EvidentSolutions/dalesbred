package fi.evident.dalesbred.results;

import fi.evident.dalesbred.instantiation.Instantiator;
import fi.evident.dalesbred.instantiation.InstantiatorRegistry;
import fi.evident.dalesbred.instantiation.NamedTypeList;
import fi.evident.dalesbred.utils.ResultSetUtils;
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
    private final InstantiatorRegistry instantiatorRegistry;

    private ReflectionResultSetProcessor(@NotNull Class<T> cl, @NotNull InstantiatorRegistry instantiatorRegistry) {
        this.cl = requireNonNull(cl);
        this.instantiatorRegistry = requireNonNull(instantiatorRegistry);
    }
    
    public static <T> ReflectionResultSetProcessor<T> forClass(@NotNull Class<T> cl, @NotNull InstantiatorRegistry instantiatorRegistry) {
        return new ReflectionResultSetProcessor<T>(cl, instantiatorRegistry);
    }

    @Override
    public List<T> process(ResultSet resultSet) throws SQLException {
        Instantiator<T> ctor = findInstantiator(resultSet.getMetaData());

        ArrayList<T> result = new ArrayList<T>();

        Object[] args = new Object[resultSet.getMetaData().getColumnCount()];

        while (resultSet.next()) {
            for (int i = 0; i < args.length; i++)
                args[i] = resultSet.getObject(i+1);

            result.add(ctor.instantiate(args));
        }

        return result;
    }

    private Instantiator<T> findInstantiator(ResultSetMetaData metaData) throws SQLException {
        NamedTypeList types = ResultSetUtils.getTypes(metaData);
        return instantiatorRegistry.findInstantiator(cl, types);
    }
}
