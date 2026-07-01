package org.dalesbred.internal.utils;

import org.dalesbred.DatabaseException;
import org.jetbrains.annotations.NotNull;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public final class JndiUtils {

    private JndiUtils() {
    }

    public static @NotNull DataSource lookupJndiDataSource(@NotNull String jndiName) {
        try {
            InitialContext ctx = new InitialContext();
            try {
                DataSource dataSource = (DataSource) ctx.lookup(jndiName);
                if (dataSource != null)
                    return dataSource;
                else
                    throw new DatabaseException("Could not find DataSource '" + jndiName + '\'');
            } finally {
                ctx.close();
            }
        } catch (NamingException e) {
            throw new DatabaseException("Error when looking up DataSource '" + jndiName + "': " + e, e);
        }
    }
}
