package org.dalesbred.integration.spring;

import org.dalesbred.connection.ConnectionProvider;
import org.dalesbred.dialect.Dialect;
import org.dalesbred.transaction.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static java.util.Objects.requireNonNull;

/**
 * <p>
 * {@link ConnectionProvider} which integrates with Spring's transaction management.
 * </p>
 * <p>
 * Usually application code should not need this class, but use {@link DalesbredConfigurationSupport} to
 * integrate with Spring.
 * </p>
 */
public final class SpringTransactionManager implements TransactionManager {

    private final @NotNull DataSource dataSource;

    private final @NotNull PlatformTransactionManager platformTransactionManager;

    /**
     * Constructs new SpringTransactionManager to use.
     */
    public SpringTransactionManager(@NotNull DataSource dataSource, @NotNull PlatformTransactionManager platformTransactionManager) {
        this.dataSource = requireNonNull(dataSource);
        this.platformTransactionManager = requireNonNull(platformTransactionManager);
    }

    @Override
    public <T> T withCurrentTransaction(@NotNull TransactionCallback<T> callback, @NotNull Dialect dialect) {
        return execute(callback, dialect, new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_MANDATORY));
    }

    @Override
    public <T> T withTransaction(@NotNull TransactionSettings settings, @NotNull TransactionCallback<T> callback, @NotNull Dialect dialect) {
        return execute(callback, dialect, settingsToSpringDefinition(settings));
    }

    private <T> T execute(@NotNull TransactionCallback<T> callback, @NotNull Dialect dialect, @NotNull DefaultTransactionDefinition df) {
        TransactionTemplate tt = new TransactionTemplate(platformTransactionManager, df);
        return tt.execute(status -> {
            try {
                Connection connection = DataSourceUtils.getConnection(dataSource);
                try {
                    return callback.execute(new SpringTransactionContext(status, connection));
                } finally {
                    DataSourceUtils.releaseConnection(connection, dataSource);
                }
            } catch (SQLException e) {
                throw dialect.convertException(e);
            }
        });
    }

    @Override
    public boolean hasActiveTransaction() {
        ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
        return conHolder != null && (conHolder.getConnectionHandle() != null || conHolder.isSynchronizedWithTransaction());
    }

    static int springIsolationCode(@NotNull Isolation isolation) {
        if (isolation == Isolation.DEFAULT)
            return TransactionDefinition.ISOLATION_DEFAULT;
        else
            return isolation.getJdbcLevel();
    }

    static int springPropagationCode(@NotNull Propagation propagation) {
        return switch (propagation) {
            case REQUIRED -> TransactionDefinition.PROPAGATION_REQUIRED;
            case MANDATORY -> TransactionDefinition.PROPAGATION_MANDATORY;
            case NESTED -> TransactionDefinition.PROPAGATION_NESTED;
            case REQUIRES_NEW -> TransactionDefinition.PROPAGATION_REQUIRES_NEW;
        };
    }

    private static @NotNull DefaultTransactionDefinition settingsToSpringDefinition(@NotNull TransactionSettings settings) {
        DefaultTransactionDefinition df = new DefaultTransactionDefinition();
        df.setIsolationLevel(springIsolationCode(settings.getIsolation()));
        df.setPropagationBehavior(springPropagationCode(settings.getPropagation()));
        return df;
    }
}
