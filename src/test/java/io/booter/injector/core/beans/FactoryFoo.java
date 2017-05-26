package io.booter.injector.core.beans;

import javax.annotation.PostConstruct;

public class FactoryFoo {
    private final Bar bar;
    private final Ber ber;
    private boolean visited = false;

    public FactoryFoo(Bar bar, Ber ber) {
        this.bar = bar;
        this.ber = ber;
    }

    public Bar bar() {
        return bar;
    }

    public Ber ber() {
        return ber;
    }

    @PostConstruct
    void visit() {
        visited = true;
    }

    public boolean isVisited() {
        return visited;
    }
}
