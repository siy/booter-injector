package io.booter.injector.core.supplier;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static io.booter.injector.core.supplier.Utils.*;

//TODO: refactor suppliers to be algorithm-only. others merge with supplier factory. supplier factory should be static
public final class Suppliers {
    private Suppliers() {
    }

    public static <T> Supplier<T> factoryLazy(final Supplier<Supplier<T>> factory) {
        validateNotNull(factory);

        return new Supplier<T>() {
            private final Supplier<T> defaultDelegate = () -> init();
            private final AtomicBoolean marker = new AtomicBoolean();
            private Supplier<T> delegate = defaultDelegate;

            private T init() {
                if (marker.compareAndSet(false, true)) {
                    delegate = factory.get();
                } else {
                    while (delegate == defaultDelegate) {
                        //Intentionally left empty
                    }
                }
                return delegate.get();
            }

            public T get() {
                return delegate.get();
            }
        };
    }

    public static <T> Supplier<T> lazy(final Supplier<T> factory) {
        validateNotNull(factory);
        return factoryLazy(() -> {T instance = factory.get(); return () -> instance;});
    }

    public static <T> Supplier<T> singleton(final Supplier<T> factory, boolean eager) {
        validateNotNull(factory);

        if (eager) {
            T instance = factory.get();
            return () -> instance;
        }
        return lazy(factory);
    }

    //TODO: createInstanceSupplier generalized implementation?
    public static <T> Supplier<T> enhancing(final Supplier<T> initial, Supplier<Supplier<T>> enhanced) {
        validateNotNull(initial, enhanced);

        return new Supplier<T>() {
            private Supplier<T> delegate = () -> step(() -> () -> step(() -> () -> step(enhanced)));

            private T step(Supplier<Supplier<T>> next) {
                T instance = initial.get();
                delegate = next.get();
                return instance;
            }

            @Override
            public T get() {
                return delegate.get();
            }
        };
    }
}