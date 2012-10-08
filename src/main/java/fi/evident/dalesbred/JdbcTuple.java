package fi.evident.dalesbred;

import java.util.List;

public interface JdbcTuple {
    public int getColumnsAmount();
    public List<?> getObjectsInOrder();
}
