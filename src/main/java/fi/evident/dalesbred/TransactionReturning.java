package fi.evident.dalesbred;

public interface TransactionReturning<T> {
    T execute();
}
