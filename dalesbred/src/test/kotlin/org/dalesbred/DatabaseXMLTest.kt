package org.dalesbred

import org.dalesbred.testutils.DatabaseProvider.POSTGRESQL
import org.dalesbred.testutils.DatabaseTest
import org.dalesbred.testutils.transactionalTest
import org.intellij.lang.annotations.Language
import org.w3c.dom.Document
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DatabaseTest(POSTGRESQL)
class DatabaseXMLTest(private val db: Database) {

    @Test
    fun `converting between DOM nodes and SQLXML`() = transactionalTest(db) {
        db.update("drop table if exists xml_test")
        db.update("create temporary table xml_test (xml_document xml)")

        db.update("insert into xml_test (xml_document) values (?)", xmlDocument("<foo>bar</foo>"))

        val root = assertNotNull(db.findUnique(Document::class.java, "select xml_document from xml_test")).documentElement

        assertEquals("foo", root.tagName)
        assertEquals("bar", root.textContent)
    }

    private fun xmlDocument(@Language("XML") xml: String) =
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml.byteInputStream())
}
