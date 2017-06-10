package io.booter.injector.annotations;

import java.lang.annotation.*;

/**
 * This annotation is used to provide reference to implementation from interface. For example:
 * <pre>{@code
 * @ImplementedBy(MyInterfaceImpl.class)
 * public interface MyInterface {
 *     ...
 * }
 *
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface ImplementedBy {
    Class<?> value();
}
