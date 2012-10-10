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
}
