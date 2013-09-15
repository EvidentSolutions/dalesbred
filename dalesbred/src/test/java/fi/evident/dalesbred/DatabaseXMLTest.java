package fi.evident.dalesbred;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class DatabaseXMLTest {

    private final Database db = TestDatabaseProvider.createPostgreSQLDatabase();

    @Rule
    public final TransactionalTestsRule transactionalTests = new TransactionalTestsRule(db);

    @Test
    public void convertingBetweenDomNodesAndSQLXML() throws Exception {
        db.update("drop table if exists xml_test");
        db.update("create temporary table xml_test (xml_document xml)");

        db.update("insert into xml_test (xml_document) values (?)", xmlDocument("<foo>bar</foo>"));

        Document xml = db.findUnique(Document.class, "select xml_document from xml_test");

        assertThat(xml, is(notNullValue()));
        Element root = xml.getDocumentElement();
        assertThat(root.getTagName(), is("foo"));
        assertThat(root.getTextContent(), is("bar"));
    }

    @NotNull
    private static Document xmlDocument(@NotNull @Language("XML") String xml) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }
}
