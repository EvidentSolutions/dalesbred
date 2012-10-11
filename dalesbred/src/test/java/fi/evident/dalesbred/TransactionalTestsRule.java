package fi.evident.dalesbred;

import fi.evident.dalesbred.connection.DriverManagerConnectionProvider;
import fi.evident.dalesbred.dialects.Dialect;
import org.jetbrains.annotations.NotNull;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.inject.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assume.assumeNotNull;

final class TransactionalTestsRule implements TestRule {

    public final Database db;

    public TransactionalTestsRule(String properties) {
        db = new Database(createConnectionProvider(properties));
        db.setAllowImplicitTransactions(false);
    }

    public TransactionalTestsRule(@NotNull String properties, @NotNull Dialect dialect) {
        db = new Database(createConnectionProvider(properties), dialect);
        db.setAllowImplicitTransactions(false);
    }

    private Provider<Connection> createConnectionProvider(@NotNull String properties) {
        Properties props = loadConnectionProperties(properties);
        String url = props.getProperty("jdbc.url");
        String login = props.getProperty("jdbc.login");
        String password = props.getProperty("jdbc.password");

        return new DriverManagerConnectionProvider(url, login, password);
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Throwable throwable =
                    db.withTransaction(new ConnectionCallback<Throwable>() {
                        @Override
                        public Throwable execute(@NotNull Connection connection) throws SQLException {
                            try {
                                base.evaluate();
                                return null;
                            } catch (Throwable throwable) {
                                connection.rollback();
                                return throwable;
                            }
                        }
                    });
                if (throwable != null)
                    throw throwable;
            }
        };
    }

    private Properties loadConnectionProperties(String propertiesName) {
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream(propertiesName);
            assumeNotNull(in);
            try {
                Properties properties = new Properties();
                properties.load(in);
                return properties;
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
