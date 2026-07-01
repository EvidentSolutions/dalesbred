package org.dalesbred.internal.result;

import org.dalesbred.internal.instantiation.Instantiator;
import org.dalesbred.internal.instantiation.InstantiatorArguments;
import org.dalesbred.internal.instantiation.InstantiatorProvider;
import org.dalesbred.internal.instantiation.NamedTypeList;
import org.dalesbred.internal.jdbc.ResultSetUtils;
import org.dalesbred.result.RowMapper;
import org.dalesbred.result.UnexpectedResultException;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

import static java.util.Objects.requireNonNull;

public final class InstantiatorRowMapper<T> implements RowMapper<T> {

    private final @NotNull Class<T> cl;

    private final @NotNull InstantiatorProvider instantiatorProvider;

    private NamedTypeList types;

    private Instantiator<T> ctor;

    // For performance reasons we reuse the same arguments-array and InstantiatorArguments-object for all rows.
    // This should be fine as long as the instantiators don't hang on to their arguments for too long.
    private Object[] arguments;

    private InstantiatorArguments instantiatorArguments;

    public InstantiatorRowMapper(@NotNull Class<T> cl, @NotNull InstantiatorProvider instantiatorProvider) {
        this.cl = requireNonNull(cl);
        this.instantiatorProvider = requireNonNull(instantiatorProvider);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public T mapRow(@NotNull ResultSet resultSet) throws SQLException {
        if (types == null) {
            types = ResultSetUtils.getTypes(resultSet.getMetaData(), instantiatorProvider.getDialect());
            ctor = instantiatorProvider.findInstantiator(cl, types);
            arguments = new Object[types.size()];
            instantiatorArguments = new InstantiatorArguments(types, arguments);
        }

        boolean allowNulls = !cl.isPrimitive();

        for (int i = 0; i < arguments.length; i++)
            arguments[i] = resultSet.getObject(i+1);

        T value = ctor.instantiate(instantiatorArguments);
        if (value != null || allowNulls)
            return value;
        else
            throw new UnexpectedResultException("Expected " + cl.getName() + ", but got null");
    }
}
