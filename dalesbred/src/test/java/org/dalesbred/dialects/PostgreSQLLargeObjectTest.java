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

package org.dalesbred.dialects;

import org.dalesbred.Database;
import org.dalesbred.TestDatabaseProvider;
import org.dalesbred.TransactionalTestsRule;
import org.dalesbred.lob.InputStreamWithSize;
import org.dalesbred.lob.ReaderWithSize;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PostgreSQLLargeObjectTest {

    private final Database db = TestDatabaseProvider.createPostgreSQLDatabase();

    @Rule
    public final TransactionalTestsRule rule = new TransactionalTestsRule(db);

    @Test
    @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
    public void streamBlobToDatabaseByteArray() throws Exception {
        db.update("drop table if exists blob_test");
        db.update("create temporary table blob_test (id int, blob_data bytea)");

        byte[] originalData = { 25, 35, 3 };
        db.update("insert into blob_test values (1, ?)", new InputStreamWithSize(new ByteArrayInputStream(originalData), originalData.length));

        byte[] data = db.findUnique(byte[].class, "select blob_data from blob_test where id=1");
        assertThat(data, is(originalData));
    }

    @Test
    @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
    public void streamReaderToDatabaseText() throws Exception {
        db.update("drop table if exists text_test");
        db.update("create temporary table text_test (id int, text_data text)");

        String originalData = "foo";
        db.update("insert into text_test values (1, ?)", new ReaderWithSize(new StringReader(originalData), originalData.length()));

        String data = db.findUnique(String.class, "select text_data from text_test where id=1");
        assertThat(data, is(originalData));
    }
}
