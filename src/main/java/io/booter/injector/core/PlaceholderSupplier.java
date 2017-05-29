package io.booter.injector.core;

import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.function.Supplier;

class PlaceholderSupplier<T> implements Supplier<T> {
    private final LazyInjector injector;
    private final Key key;
    private final Supplier<T> defaultDelegate = () -> bindParameters();
    private Supplier<T> delegate = defaultDelegate;

    private T bindParameters() {
        return delegate().get();
    }

    public PlaceholderSupplier(LazyInjector injector, Key key) {
        this.injector = injector;
        this.key = key;
    }

    @Override
    public T get() {
        return delegate.get();
    }

    public Supplier<T> delegate() {
        synchronized (defaultDelegate) {
            if (delegate == defaultDelegate) {
                return (delegate = injector.collectBindings(key));
            }
        }
        return delegate;
    }
}
