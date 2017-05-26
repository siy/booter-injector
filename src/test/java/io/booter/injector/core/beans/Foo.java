package io.booter.injector.core.beans;

import io.booter.injector.annotations.Inject;
import io.booter.injector.annotations.Singleton;

@Singleton
public class Foo {
    private final Bar bar;
    private final Foe foe;

    @Inject
    public Foo(Bar bar, Foe foe) {
        this.bar = bar;
        this.foe = foe;
    }

    public Bar bar() {
        return bar;
    }

    public Foe foe() {
        return foe;
    }
}
