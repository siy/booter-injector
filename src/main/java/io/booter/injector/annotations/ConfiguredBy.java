package io.booter.injector.annotations;

import java.lang.annotation.*;

/**
 * This annotation provides reference to class which can be used for configuration of annotated class. For example:
 * <pre>{@code
 *
 * @ConfiguredBy(MyModule.class)
 * public MyClass {
 *     ...
 * }
 * }</pre>
 *
 * In the example above class <code>MyModule</code> contains configuration information for the <code>MyClass</code>.
 * Configuration class can provide binding information in two ways:
 * <ol>
 *     <li>Via methods annotated with {@link Supplies} annotation</li>
 *     <li>Via implementing {@link io.booter.injector.Module} interface</li>
 * </ol>
 * These methods are not mutually exclusive, so they can be used together where necessary.
 * <br />
 * Instances of classes referenced by the annotation are created through injector, so they are handled just like any
 * other class - their dependencies will be injected. There is only one significant difference:
 * {@link ConfiguredBy} annotation is ignored for configuration class itself. In the example above class
 * <code>MyModule</code> will be loaded without taking into account {@link ConfiguredBy} annotation.
 *
 * @see Supplies
 * @see io.booter.injector.Module
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface ConfiguredBy {
    /**
     * @return Configuration class.
     */
    Class<?> value();
}
