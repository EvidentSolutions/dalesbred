/*
 * Copyright (c) 2017 Evident Solutions Oy
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

package org.dalesbred

import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.Test
import org.w3c.dom.Document
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DatabaseXMLTest {

    private val db = TestDatabaseProvider.createPostgreSQLDatabase()

    @get:Rule val rule = TransactionalTestsRule(db)

    @Test
    fun convertingBetweenDomNodesAndSQLXML() {
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
