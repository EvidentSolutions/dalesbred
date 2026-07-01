package org.dalesbred.conversion;

/**
 * Interface that contains conversions both to database and back.
 */
public interface TypeConversionPair<D,J> {
    D convertToDatabase(J obj);
    J convertFromDatabase(D obj);
}
