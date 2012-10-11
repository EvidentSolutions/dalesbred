package fi.evident.dalesbred;

/**
 * Transaction propagation types.
 */
public enum Propagation {

    /** Join existing transaction if there is one, otherwise create a new one. */
    REQUIRED,

    /** Join existing transaction if there is one, otherwise throw an exception. */
    MANDATORY,

    /** Always create a new transaction. Existing transaction is suspended for the duration of this transaction. */
    REQUIRES_NEW,

    /** Start a nested transaction if there is a current transaction, otherwise start a new normal transaction. */
    NESTED,
}
