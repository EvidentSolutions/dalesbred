package fi.evident.dalesbred;

import fi.evident.dalesbred.connection.DriverManagerConnectionProvider;
import fi.evident.dalesbred.dialects.Dialect;
import org.jetbrains.annotations.NotNull;

import javax.inject.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;

import static org.junit.Assume.assumeNotNull;

public class TestDatabaseProvider {

    public static Database createTestDatabase() {
        return new Database(createConnectionProvider("connection.properties"));
    }

    public static Database createTestDatabase(Dialect dialect) {
        return new Database(createConnectionProvider("connection.properties"), dialect);
    }

    private static Provider<Connection> createConnectionProvider(@NotNull String properties) {
        Properties props = loadConnectionProperties(properties);
        String url = props.getProperty("jdbc.url");
        String login = props.getProperty("jdbc.login");
        String password = props.getProperty("jdbc.password");

        return new DriverManagerConnectionProvider(url, login, password);
    }

    private static Properties loadConnectionProperties(String propertiesName) {
        try {
            InputStream in = TransactionCallback.class.getClassLoader().getResourceAsStream(propertiesName);
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
