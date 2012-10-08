package fi.evident.dalesbred.instantiation;

public interface Instantiator<T> {
    T instantiate(Object[] arguments, Coercions coercions);
    int getCost();
}
