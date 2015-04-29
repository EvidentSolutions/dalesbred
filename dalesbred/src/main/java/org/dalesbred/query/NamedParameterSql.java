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

package org.dalesbred.query;

import org.dalesbred.SQL;
import org.dalesbred.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;
import java.util.List;

final class NamedParameterSql {

    @NotNull
    @SQL
    private final String sql;

    @NotNull
    private final List<String> parameterNames;

    NamedParameterSql(@NotNull @SQL String sql, @NotNull List<String> parameterNames) {
        this.sql = sql;
        this.parameterNames = parameterNames;
    }

    @NotNull
    public SqlQuery toQuery(@NotNull VariableResolver variableResolver) {
        return SqlQuery.query(sql, resolveParameterValues(variableResolver));
    }

    @NotNull
    private List<?> resolveParameterValues(@NotNull VariableResolver variableResolver) {
        List<Object> result = new ArrayList<>(parameterNames.size());

        for (String parameter : parameterNames) {
            result.add(variableResolver.getValue(parameter));
        }

        return result;
    }

    @NotNull
    @SQL
    @TestOnly
    String getSql() {
        return sql;
    }

    @NotNull
    @TestOnly
    public List<String> getParameterNames() {
        return parameterNames;
    }
}
