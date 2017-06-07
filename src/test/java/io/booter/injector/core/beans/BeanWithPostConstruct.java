package io.booter.injector.core.beans;

import io.booter.injector.core.beans.tree.Bar;
import io.booter.injector.core.beans.tree.Ber;

import javax.annotation.PostConstruct;

public class BeanWithPostConstruct {
    private final Bar bar;
    private final Ber ber;
    private boolean invoked = false;

    public BeanWithPostConstruct(Bar bar, Ber ber) {
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
    public void invoked() {
        this.invoked = true;
    }

    public boolean isInvoked() {
        return invoked;
    }
}
