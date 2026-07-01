package org.dalesbred.query;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.util.List;

import static org.dalesbred.internal.utils.CollectionUtils.mapToList;

record NamedParameterSql(
    @Language("SQL") @NotNull String sql,
    @NotNull List<String> parameterNames
) {

    public @NotNull SqlQuery toQuery(@NotNull VariableResolver variableResolver) {
        return SqlQuery.query(sql, resolveParameterValues(variableResolver));
    }

    private @NotNull List<?> resolveParameterValues(@NotNull VariableResolver variableResolver) {
        return mapToList(parameterNames, variableResolver::getValue);
    }

    @Override
    @NotNull
    @Language("SQL")
    @TestOnly
    public String sql() {
        return sql;
    }

    @Override
    @TestOnly
    public @NotNull List<String> parameterNames() {
        return parameterNames;
    }
}
