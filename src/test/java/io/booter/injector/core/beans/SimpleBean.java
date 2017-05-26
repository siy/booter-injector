package io.booter.injector.core.beans;

public class SimpleBean {
    private final Bar bar;
    private final Ber ber;

    public SimpleBean(Bar bar, Ber ber) {
        this.bar = bar;
        this.ber = ber;
    }

    public Bar bar() {
        return bar;
    }

    public Ber ber() {
        return ber;
    }
}
