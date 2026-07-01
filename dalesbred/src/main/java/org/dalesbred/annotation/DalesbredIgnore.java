package org.dalesbred.annotation;

import java.lang.annotation.*;

/**
 * Ignores given field, setter or constructor when instantiating.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR })
@Inherited
public @interface DalesbredIgnore {
}
