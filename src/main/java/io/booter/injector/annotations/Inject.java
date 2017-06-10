package io.booter.injector.annotations;

import java.lang.annotation.*;

/**
 * This annotation is used to mark constructor which should be used by injector. Only one constructor can be marked
 * with this annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR})
@Documented
public @interface Inject {
}
