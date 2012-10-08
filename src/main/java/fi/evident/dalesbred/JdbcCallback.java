package fi.evident.dalesbred;

import java.sql.SQLException;

public interface JdbcCallback<I,O> {
    O execute(I i) throws SQLException;
}
