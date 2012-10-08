package fi.evident.dalesbred.example;

import fi.evident.dalesbred.AbstractJdbcDao;
import fi.evident.dalesbred.JdbcOperations;
import fi.evident.dalesbred.connection.DriverManagerConnectionProvider;

import javax.inject.Provider;
import java.sql.Connection;

public class Main extends AbstractJdbcDao {

    public void run() {
        System.out.println(queryForInt(query("select 42")));
    }

    public static void main(String[] args) {
        Provider<Connection> connectionProvider = new DriverManagerConnectionProvider("jdbc:postgresql://db/dalesbred-test", "evident", "Evident11");
        Main main = new Main();
        main.setJdbcOperations(new JdbcOperations(connectionProvider));
        main.run();
    }
}
