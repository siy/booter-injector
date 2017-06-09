package io.booter.injector;

import io.booter.injector.core.ScanningInjector;

import java.util.function.Supplier;

/**
 * Injector API interface.
 */
public interface Injector {
    /**
     * Get or create instance for specified class. Note that it might be instance of another class depending on the
     * configuration of particular binding. If no satisfying binding is found then Injector attempts to build dependency
     * tree and instantiate class. If dependencies cannot be resolved then
     * {@link io.booter.injector.core.exception.InjectorException} is thrown.
     *
     * @param clazz
     *          Class for which instance should be created.
     * @return created instance
     */
    <T> T get(Class<T> clazz);

    /**
     * Get or create instance of class bound to given key. If no satisfying binding is found then Injector attempts to
     * build dependency tree and instantiate class. If dependencies cannot be resolved then
     * {@link io.booter.injector.core.exception.InjectorException} is thrown.
     *
     * @param key
     *          Key for which instance should be created.
     * @return created instance
     */
    <T> T get(Key key);

    /**
     * Get or create instance of {@link Supplier} which returns instances for specified class. Note that it might be
     * instance of another class depending on the configuration of particular binding. If no satisfying binding is
     * found then Injector attempts to build dependency tree and instantiate class. If dependencies cannot be resolved
     * then {@link io.booter.injector.core.exception.InjectorException} is thrown.

     * @param clazz
     *          Class for which {@link Supplier} should be provided.
     * @return supplier for class instances (or substituting class instances)
     */
    <T> Supplier<T> supplier(Class<T> clazz);

    /**
     * Get or create instance of {@link Supplier} bound to given key. If no satisfying binding is
     * found then Injector attempts to build dependency tree and create supplier. If dependencies cannot be resolved
     * then {@link io.booter.injector.core.exception.InjectorException} is thrown.

     * @param key
     *          Key for which supplier should be returned.
     * @return supplier for class instances (or substituting class instances)
     */
    <T> Supplier<T> supplier(Key key);

    /**
     * Configure {@link Injector} with specified classes. Each class can provide two variants of configuration data:
     * class may implement {@link Module} interface (or be a sublass of {@link AbstractModule}) and/or describe
     * bindings as methods annotated with {@link io.booter.injector.annotations.Supplies} annotation. Example of both
     * types of configuration:
     *
     * <pre>{@code
     *  public class MyModule extends AbstractModule {
     *
     *      public void configure() {
     *          bind(MyInterface.class).toSingleton(MyImplementation.class);
     *          bind(MyOtherInterface.class).toEagerSingleton(MyOtherImplementation.class);
     *      }
     *
     *      @Supplies
     *      @MyAnnotation
     *      public List<String> names() {
     *          return Arrays.asList("John", "Pete");
     *      }
     *  }
     * }</pre>
     *
     * Note that if method has annotation which in turn is annotated with
     * {@link io.booter.injector.annotations.BindingAnnotation} then this annotation becomes part of the binding
     * and only requests for annotated instances will match this binding.
     *
     * @see io.booter.injector.annotations.BindingAnnotation
     * @param configurators
     *          Classes with configuration information.
     * @return <code>this</code> for call chaining (fluent syntax).
     */
    Injector configure(Class<?>... configurators);

    /**
     * Static factory for {@link Injector} instances.
     *
     * @return created injector.
     */
    static Injector create() {
        return new ScanningInjector();
    }

    /**
     * Static factory for {@link Injector} instances. Injector is returned pre-configured with specified configuration
     * classes.
     *
     * @return created and configured injector.
     */
    static Injector create(Class<?>...classes) {
        return new ScanningInjector().configure(classes);
    }
}
