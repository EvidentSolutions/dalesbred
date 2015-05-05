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

    @NotNull
    private final Class<T> cl;

    @NotNull
    private final InstantiatorProvider instantiatorProvider;

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

    @Override
    public T mapRow(@NotNull ResultSet resultSet) throws SQLException {
        if (types == null) {
            types = ResultSetUtils.getTypes(resultSet.getMetaData());
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
