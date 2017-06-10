package io.booter.injector.annotations;

import java.lang.annotation.*;

/**
 * This annotation is used to mark methods in configuration class which will supply instances of specified type.
 * For example:
 *
 * <pre>{@code
 * ...
 *  @Supplies
 *  public MyService<Notification> createNotificationService() {
 *  ...
 *  }
 * ...
 * }</pre>
 *
 * The return type of method is the exact type of the binding. If method has any binding annotation, it will be attached
 * to return type as well. If method has parameters, they will be injected during method invocation.
 *
 * @see ConfiguredBy
 * @see BindingAnnotation
 * @see io.booter.injector.Module
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface Supplies {
}
