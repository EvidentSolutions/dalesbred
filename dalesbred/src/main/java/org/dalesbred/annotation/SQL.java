package org.dalesbred.annotation;

import org.intellij.lang.annotations.Language;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * Marks a string variable as containing SQL. IntelliJ IDEA uses this to provide syntax highlighting
 * for SQL-strings, otherwise it's mostly useful as a form of documentation.
 *
 * @deprecated IDEA no longer detects @Language from meta-annotations os use use @Language directly
 */
@Language("SQL")
@Retention(RetentionPolicy.CLASS)
@Target({ METHOD, FIELD, PARAMETER, LOCAL_VARIABLE })
@Deprecated
public @interface SQL {
}
