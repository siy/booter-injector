package io.booter.injector.annotations;

import java.lang.annotation.*;

/**
 * This is a special annotation which is used to create binding annotations from regular annotations.
 * Binding annotations are used to distinguish dependencies of same formal type but with different purposes.
 * For example:
 * <pre>{@code
 * ...
 * @Retention(RetentionPolicy.RUNTIME)
 * @Target({PARAMETER, FIELD})
 * @Documented
 * @BindingAnnotation
 * public @interface MyAnnotation {
 * }
 * ...
 *
 * @ConfiguredBy(MyModule.class)
 * public class MyClass {
 *   ...
 *   public MyClass(@MyAnnotation List&lt;String&gt; names) {
 *     ...
 *   }
 *   ...
 * }
 * ...
 * public class MyModule {
 *   ...
 *   @Supplies
 *   @MyAnnotation
 *   public List&lt;String&gt; getServiceNames() {
 *      ...
 *   }
 *   ...
 * }
 *
 * ...
 * }</pre>
 *
 * @see Supplies
 * @see ConfiguredBy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@Documented
public @interface BindingAnnotation {
}
