package io.booter.injector.core.beans;

import java.util.function.Supplier;

import io.booter.injector.annotations.Inject;
import io.booter.injector.annotations.Singleton;

@Singleton
public class Foe {
    private final Supplier<Foo> parent;

    @Inject
    public Foe(Supplier<Foo> parent) {
        this.parent = parent;
    }

    public Supplier<Foo> parent() {
        return parent;
    }
}
