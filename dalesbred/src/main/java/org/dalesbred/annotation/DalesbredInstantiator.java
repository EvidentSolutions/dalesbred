package org.dalesbred.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * Marks a constructor or a static method as Dalesbred instantiator. This means that when Dalesbred tries to
 * instantiate classes of this type, it skips the normal lookup resolution and will always use this constructor.
 * It will not search for other constructors, nor will it set any properties.
 * </p>
 * <p>
 * It is an error to mark multiple constructors/methods as instantiators.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface DalesbredInstantiator {
}
