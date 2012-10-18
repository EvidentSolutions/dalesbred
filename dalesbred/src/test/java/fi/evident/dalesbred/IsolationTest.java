/*
 * Copyright (c) 2012 Evident Solutions Oy
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

package fi.evident.dalesbred;

import org.junit.Test;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;

import static fi.evident.dalesbred.Isolation.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class IsolationTest {

    @Test
    public void levelsMatchJdbcLevels() {
        assertEquals(Connection.TRANSACTION_READ_UNCOMMITTED, READ_UNCOMMITTED.level);
        assertEquals(Connection.TRANSACTION_READ_COMMITTED, READ_COMMITTED.level);
        assertEquals(Connection.TRANSACTION_REPEATABLE_READ, REPEATABLE_READ.level);
        assertEquals(Connection.TRANSACTION_SERIALIZABLE, SERIALIZABLE.level);
    }

    @Test
    public void levelsAreSortedCorrectly() {
        List<Isolation> levels = asList(REPEATABLE_READ, SERIALIZABLE, READ_UNCOMMITTED, READ_COMMITTED);
        Collections.sort(levels);

        assertEquals(asList(READ_UNCOMMITTED, READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE), levels);
    }

    @Test
    public void fromJdbcIsolation() {
        assertEquals(READ_UNCOMMITTED, forJdbcCode(Connection.TRANSACTION_READ_UNCOMMITTED));
        assertEquals(READ_COMMITTED,   forJdbcCode(Connection.TRANSACTION_READ_COMMITTED));
        assertEquals(REPEATABLE_READ,  forJdbcCode(Connection.TRANSACTION_REPEATABLE_READ));
        assertEquals(SERIALIZABLE,     forJdbcCode(Connection.TRANSACTION_SERIALIZABLE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromInvalidJdbcIsolation() {
        forJdbcCode(525);
    }
}
