package fi.evident.dalesbred;

import org.junit.Rule;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BuiltinCoercionsTest {

    @Rule
    public final TransactionalTestsRule rule = new TransactionalTestsRule("connection.properties");

    private final Database db = rule.db;

    @Test
    public void urlsAndUris() throws MalformedURLException, URISyntaxException {
        db.update("drop table if exists url_and_uri");
        db.update("create table url_and_uri (url varchar(64), uri varchar(64))");

        URL url = new URL("http://example.org");
        URI uri = new URI("http://example.net");

        db.update("insert into url_and_uri (url, uri) values (?, ?)", url, uri);

        UrlAndUri result = db.findUnique(UrlAndUri.class, "select url, uri from url_and_uri");

        assertThat(result.url, is(url));
        assertThat(result.uri, is(uri));
    }

    public static final class UrlAndUri {
        final URL url;
        final URI uri;

        public UrlAndUri(URL url, URI uri) {
            this.url = url;
            this.uri = uri;
        }
    }
}
