/*
 * Copyright (c) 2015 Evident Solutions Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.dalesbred;

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
