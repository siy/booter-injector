package io.booter.injector;

import io.booter.injector.annotations.Named;
import io.booter.injector.core.AnnotationFactory;
import io.booter.injector.core.exception.InjectorException;

import java.util.Collections;

public final class Naming {
    private Naming() {}

    public static Named with(String name) {
        if (name == null) {
            throw new InjectorException("Valuer for @Named annotation can't be null");
        }
        return AnnotationFactory.create(Named.class, Collections.singletonMap("value", name));
    }
}
