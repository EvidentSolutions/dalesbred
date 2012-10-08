package fi.evident.dalesbred;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static fi.evident.dalesbred.SqlQuery.query;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeNotNull;

public class DatabaseTest {

    private Database db;

    @Before
    public void setupConnection() throws IOException {
        Properties props = loadConnectionProperties();
        String url = props.getProperty("jdbc.url");
        String login = props.getProperty("jdbc.login");
        String password = props.getProperty("jdbc.password");
        db = Database.forUrlAndCredentials(url, login, password);
    }

    @Test
    public void primitivesQueries() {
        assertThat(db.findUniqueInt(query("select 42")), is(42));
        assertThat(db.findUnique(query("select 42"), Integer.class), is(42));
        assertThat(db.findUnique(query("select 'foo'"), String.class), is("foo"));
        assertThat(db.findUnique(query("select true"), Boolean.class), is(true));
        assertThat(db.findUnique(query("select null::boolean"), Boolean.class), is(nullValue()));
    }

    @Test
    public void autoDetectingTypes() {
        assertThat(db.findUnique(query("select 42"), Object.class), is((Object) 42));
        assertThat(db.findUnique(query("select 'foo'"), Object.class), is((Object) "foo"));
        assertThat(db.findUnique(query("select true"), Object.class), is((Object) true));
    }

    private Properties loadConnectionProperties() throws IOException {
        InputStream in = getClass().getResourceAsStream("/connection.properties");
        assumeNotNull(in);
        try {
            Properties properties = new Properties();
            properties.load(in);
            return properties;
        } finally {
            in.close();
        }
    }
}
