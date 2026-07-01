package org.dalesbred;

import org.dalesbred.connection.ConnectionProvider;
import org.dalesbred.connection.DataSourceConnectionProvider;
import org.dalesbred.connection.DriverManagerConnectionProvider;
import org.dalesbred.conversion.TypeConversionRegistry;
import org.dalesbred.dialect.Dialect;
import org.dalesbred.internal.instantiation.InstantiatorProvider;
import org.dalesbred.internal.utils.JndiUtils;
import org.dalesbred.query.SqlQuery;
import org.dalesbred.transaction.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.DriverManager;

import static java.util.Objects.requireNonNull;
import static org.dalesbred.transaction.TransactionCallback.fromVoidCallback;


/**
 * <p>
 * The main abstraction of the library: represents a configured connection to database and provides a way to
 * execute callbacks in transactions.
 * </p>
 * <p>
 * Usually you'll need only single instance of this in your application, unless you need to connect
 * several different databases or need different default settings for different use cases.
 * </p>
 */
public final class Database extends DatabaseAccess {

    /** Class responsible for transaction handling */
    private final @NotNull TransactionManager transactionManager;

    /** Should we create transactions implicitly when individual operations are invoked outside transaction */
    private boolean allowImplicitTransactions = true;

    /**
     * Returns a new Database that uses given {@link DataSource} to retrieve connections.
     */
    public static @NotNull Database forDataSource(@NotNull DataSource dataSource) {
        return new Database(new DataSourceConnectionProvider(dataSource));
    }

    /**
     * Returns a new Database that uses {@link DataSource} with given JNDI-name.
     */
    public static @NotNull Database forJndiDataSource(@NotNull String jndiName) {
        return forDataSource(JndiUtils.lookupJndiDataSource(jndiName));
    }

    /**
     * Returns a new Database that uses given connection options to open connection. The database
     * opens connections directly from {@link DriverManager} without performing connection pooling.
     *
     * @see DriverManagerConnectionProvider
     */
    public static @NotNull Database forUrlAndCredentials(@NotNull String url, @Nullable String username, @Nullable String password) {
        return new Database(new DriverManagerConnectionProvider(url, username, password));
    }

    /**
     * Constructs a new Database that uses given {@link ConnectionProvider} and auto-detects the dialect to use.
     */
    public Database(@NotNull ConnectionProvider connectionProvider) {
        this(connectionProvider, Dialect.detect(connectionProvider));
    }

    /**
     * Constructs a new Database that uses given {@link ConnectionProvider} and {@link Dialect}.
     */
    public Database(@NotNull ConnectionProvider connectionProvider, @NotNull Dialect dialect) {
        this(new DefaultTransactionManager(connectionProvider), dialect);
    }

    /**
     * Constructs a new Database that uses given {@link TransactionManager} and auto-detects the dialect to use.
     */
    public Database(@NotNull TransactionManager transactionManager) {
        this(transactionManager, Dialect.detect(transactionManager));
    }

    /**
     * Constructs a new Database that uses given {@link TransactionManager} and {@link Dialect}.
     */
    public Database(@NotNull TransactionManager transactionManager, @NotNull Dialect dialect) {
        super(dialect, new InstantiatorProvider(dialect));
        this.transactionManager = requireNonNull(transactionManager);

        dialect.registerTypeConversions(instantiatorRegistry.getTypeConversionRegistry());
    }

    /**
     * Constructs a new Database that uses given {@link DataSource} and auto-detects the dialect to use.
     */
    public Database(@NotNull DataSource dataSource) {
        this(new DataSourceConnectionProvider(dataSource));
    }

    /**
     * Constructs a new Database that uses given {@link DataSource} and {@link Dialect}.
     */
    public Database(@NotNull DataSource dataSource, @NotNull Dialect dialect) {
        this(new DataSourceConnectionProvider(dataSource), dialect);
    }

    /**
     * Executes a block of code within a context of a transaction, using {@link Propagation#REQUIRED} propagation.
     */
    public <T> T withTransaction(@NotNull TransactionCallback<T> callback) {
        return withTransaction(Propagation.REQUIRED, Isolation.DEFAULT, callback);
    }

    /**
     * Executes a block of code with given propagation and configuration default isolation.
     */
    public <T> T withTransaction(@NotNull Propagation propagation, @NotNull TransactionCallback<T> callback) {
        return withTransaction(propagation, Isolation.DEFAULT, callback);
    }

    /**
     * Executes a block of code with given isolation.
     */
    public <T> T withTransaction(@NotNull Isolation isolation, @NotNull TransactionCallback<T> callback) {
        return withTransaction(Propagation.REQUIRED, isolation, callback);
    }

    /**
     * Executes a block of code with given propagation and isolation.
     */
    public <T> T withTransaction(@NotNull Propagation propagation,
                                 @NotNull Isolation isolation,
                                 @NotNull TransactionCallback<T> callback) {

        TransactionSettings settings = new TransactionSettings();
        settings.setPropagation(propagation);
        settings.setIsolation(isolation);

        return withTransaction(settings, callback);
    }

    /**
     * Executes a block of code with given transaction settings.
     *
     * @see TransactionSettings
     */
    public <T> T withTransaction(@NotNull TransactionSettings settings,
                                 @NotNull TransactionCallback<T> callback) {

        return transactionManager.withTransaction(settings, callback, dialect);
    }

    /**
     * Executes a block of code within a context of a transaction, using {@link Propagation#REQUIRED} propagation.
     */
    public void withVoidTransaction(@NotNull VoidTransactionCallback callback) {
        withTransaction(fromVoidCallback(callback));
    }

    /**
     * Executes a block of code with given propagation and configuration default isolation.
     */
    public void withVoidTransaction(@NotNull Propagation propagation, @NotNull VoidTransactionCallback callback) {
        withVoidTransaction(propagation, Isolation.DEFAULT, callback);
    }

    /**
     * Executes a block of code with given isolation.
     */
    public void withVoidTransaction(@NotNull Isolation isolation, @NotNull VoidTransactionCallback callback) {
        withVoidTransaction(Propagation.REQUIRED, isolation, callback);
    }

    /**
     * Executes a block of code with given propagation and isolation.
     */
    public void withVoidTransaction(@NotNull Propagation propagation,
                                    @NotNull Isolation isolation,
                                    @NotNull VoidTransactionCallback callback) {
        withTransaction(propagation, isolation, fromVoidCallback(callback));
    }

    /**
     * Executes a block of code with given transaction settings.
     *
     * @see TransactionSettings
     */
    public void withVoidTransaction(@NotNull TransactionSettings settings,
                                    @NotNull VoidTransactionCallback callback) {
        withTransaction(settings, fromVoidCallback(callback));
    }

    /**
     * Returns true if and only if the current thread has an active transaction for this database.
     */
    public boolean hasActiveTransaction() {
        return transactionManager.hasActiveTransaction();
    }

    /**
     * Executes the block of code within context of current transaction. If there's no transaction in progress
     * throws {@link NoActiveTransactionException} unless implicit transaction are allowed: in this case, starts a new
     * transaction.
     *
     * @throws NoActiveTransactionException if there's no active transaction.
     * @see #setAllowImplicitTransactions(boolean)
     */
    @Override
    protected <T> T withCurrentTransaction(@NotNull SqlQuery query, @NotNull TransactionCallback<T> callback) {
        SqlQuery oldQuery = DebugContext.getCurrentQuery();
        try {
            DebugContext.setCurrentQuery(query);
            if (allowImplicitTransactions) {
                return withTransaction(callback);
            } else {
                return transactionManager.withCurrentTransaction(callback, dialect);
            }
        } finally {
            DebugContext.setCurrentQuery(oldQuery);
        }
    }

    /**
     * Returns {@link TypeConversionRegistry} that can be used to register new type-conversions.
     */
    public @NotNull TypeConversionRegistry getTypeConversionRegistry() {
        return instantiatorRegistry.getTypeConversionRegistry();
    }

    /**
     * Returns whether queries outside an active transaction will start a fresh transaction (true, the default)
     * or throw {@link NoActiveTransactionException} (false).
     */
    public boolean isAllowImplicitTransactions() {
        return allowImplicitTransactions;
    }

    /**
     * Controls whether queries outside an active transaction automatically start a fresh transaction.
     * When set to {@code false}, calling query methods without an active transaction throws
     * {@link NoActiveTransactionException}. Defaults to {@code true}.
     */
    public void setAllowImplicitTransactions(boolean allowImplicitTransactions) {
        this.allowImplicitTransactions = allowImplicitTransactions;
    }

    /**
     * Returns a string containing useful debug information about the state of this object.
     */
    @Override
    public @NotNull String toString() {
        return "Database [dialect=" + dialect + ", allowImplicitTransactions=" + allowImplicitTransactions + ']';
    }
}
