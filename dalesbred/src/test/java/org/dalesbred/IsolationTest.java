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

import org.junit.Test;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.dalesbred.Isolation.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class IsolationTest {

    @Test
    public void levelsMatchJdbcLevels() {
        assertThat(READ_UNCOMMITTED.getJdbcLevel(), is(Connection.TRANSACTION_READ_UNCOMMITTED));
        assertThat(READ_COMMITTED.getJdbcLevel(), is(Connection.TRANSACTION_READ_COMMITTED));
        assertThat(REPEATABLE_READ.getJdbcLevel(), is(Connection.TRANSACTION_REPEATABLE_READ));
        assertThat(SERIALIZABLE.getJdbcLevel(), is(Connection.TRANSACTION_SERIALIZABLE));
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

    @SuppressWarnings("MagicConstant")
    @Test(expected = IllegalArgumentException.class)
    public void fromInvalidJdbcIsolation() {
        forJdbcCode(525);
    }
}
