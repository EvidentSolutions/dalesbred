package org.dalesbred.dialect;

import oracle.jdbc.OracleConnection;
import org.dalesbred.datatype.SqlArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Support for Oracle.
 */
public class OracleDialect extends Dialect {

    @Override
    public void bindArgument(@NotNull PreparedStatement ps, int index, @Nullable Object value) throws SQLException {
        if (value instanceof SqlArray array) {
            @SuppressWarnings("resource")
            OracleConnection connection = ps.getConnection().unwrap(OracleConnection.class);
            ps.setArray(index, connection.createARRAY(array.getType(), array.getValues().toArray()));

        } else {
            super.bindArgument(ps, index, value);
        }
    }
}
