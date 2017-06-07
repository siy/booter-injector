package io.booter.injector.core.beans;

import io.booter.injector.core.beans.tree.Bar;
import io.booter.injector.core.beans.tree.Ber;

import java.util.function.Supplier;

public class BeanWithSupplierDependency {
    private final Supplier<Bar> bar;
    private final Ber ber;

    public BeanWithSupplierDependency(Supplier<Bar> bar, Ber ber) {
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
