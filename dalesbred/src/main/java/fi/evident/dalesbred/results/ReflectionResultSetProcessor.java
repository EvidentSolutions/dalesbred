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

    @NotNull
    @Override
    public List<T> process(@NotNull ResultSet resultSet) throws SQLException {
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
