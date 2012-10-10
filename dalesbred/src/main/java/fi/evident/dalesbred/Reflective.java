package fi.evident.dalesbred;

/**
 * Annotation used for marking members that are only accessed reflectively, e.g. constructors
 * of DTOs that are called by Dalesbred when building results. Can be configured as an entry-point
 * marker for analyzation tools.
 */
public @interface Reflective {
}
