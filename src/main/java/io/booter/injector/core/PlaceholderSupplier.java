package io.booter.injector.core;

import java.util.function.Supplier;

class PlaceholderSupplier<T> implements Supplier<T> {
    private final LazyInjector injector;
    private final Key key;
    private volatile Supplier<T> delegate = () -> bindParameters();

    private synchronized T bindParameters() {
        return (delegate = injector.collectBindings(key)).get();
    }

    public PlaceholderSupplier(LazyInjector injector, Key key) {
        this.injector = injector;
        this.key = key;
    }

    @Override
    public T get() {
        return delegate.get();
    }
}
