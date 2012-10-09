package fi.evident.dalesbred.results;

import fi.evident.dalesbred.instantiation.Instantiator;
import fi.evident.dalesbred.instantiation.InstantiatorRegistry;
import fi.evident.dalesbred.instantiation.NamedTypeList;
import fi.evident.dalesbred.utils.ResultSetUtils;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
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

    public ReflectionResultSetProcessor(@NotNull Class<T> cl, @NotNull InstantiatorRegistry instantiatorRegistry) {
        this.cl = requireNonNull(cl);
        this.instantiatorRegistry = requireNonNull(instantiatorRegistry);
    }

    @Override
    public List<T> process(ResultSet resultSet) throws SQLException {
        NamedTypeList types = ResultSetUtils.getTypes(resultSet.getMetaData());
        Instantiator<T> ctor = instantiatorRegistry.findInstantiator(cl, types);

        ArrayList<T> result = new ArrayList<T>();

        Object[] args = new Object[types.size()];

        while (resultSet.next()) {
            for (int i = 0; i < args.length; i++)
                args[i] = resultSet.getObject(i+1);

            result.add(ctor.instantiate(args));
        }

        return result;
    }
}
