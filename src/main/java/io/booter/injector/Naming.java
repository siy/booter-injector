package io.booter.injector;

import java.util.Collections;

import io.booter.injector.annotations.Named;
import io.booter.injector.core.annotation.AnnotationFactory;
import io.booter.injector.core.exception.InjectorException;

/**
 * Simple helper class for run-time creation of instances of {@link Named} annotation with specified value.
 */
public final class Naming {
    private Naming() {}

    /**
     * Create new {@link Named} annotation with provided value. Note that value cannot be <code>null</code>.
     *
     * @param name
     *          Annotation value. Must not be <code>null</code>.
     * @return Created annotation instance.
     */
    public static Named with(String name) {
        if (name == null) {
            throw new InjectorException("Valuer for @Named annotation can't be null");
        }
        return AnnotationFactory.create(Named.class, Collections.singletonMap("value", name));
    }
}
