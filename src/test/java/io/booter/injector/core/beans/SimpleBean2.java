package io.booter.injector.core.beans;

import java.util.function.Supplier;

public class SimpleBean2 {
    private final Supplier<Bar> bar;
    private final Ber ber;

    public SimpleBean2(Supplier<Bar> bar, Ber ber) {
        this.bar = bar;
        this.ber = ber;
    }

    public Bar bar() {
        return bar.get();
    }

    public Ber ber() {
        return ber;
    }
}
