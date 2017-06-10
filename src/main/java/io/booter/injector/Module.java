package io.booter.injector;

import java.util.List;

/**
 * Base module API.
 * <br />
 * Modules are used to configure {@link Injector}. The {@link #collectBindings()} method is invoked at run time to
 * obtain binding information. Usually it more convenient to inherit {@link AbstractModule} and use fluent style
 * binding builder API.
 * <br />
 * Note that bindings to classes returned by {@link #collectBindings()} method are handled differently than
 * other dependencies. During loading of these classes {@link io.booter.injector.annotations.ConfiguredBy} annotation
 * is ignored.
 *
 * @see AbstractModule
 */
public interface Module {
    /**
     * Return information about bindings.
     *
     * @return List of bindings.
     */
    List<Binding<?>> collectBindings();
}
