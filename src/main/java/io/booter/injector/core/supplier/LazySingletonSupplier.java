package io.booter.injector.core.supplier;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

import io.booter.injector.core.MethodHandleFactory;

class LazySingletonSupplier<T> extends AbstractSupplier<T> {
    private volatile Supplier<T> instance = () -> lazyInstanceCreator();

    private synchronized T lazyInstanceCreator() {
        if (instance instanceof InstanceSupplier) {
            return instance.get();
        }
        return (instance = new InstanceSupplier<>(createInstance())).get();
    }

    LazySingletonSupplier(Constructor<T> instanceConstructor, Supplier<?>[] suppliers,
                          MethodHandleFactory factory) {
        super(instanceConstructor, suppliers, factory);
    }

    @Override
    public T get() {
        return instance.get();
    }

    private static final class InstanceSupplier<T> implements Supplier<T> {
        private final T instance;

        InstanceSupplier(T instance) {
            this.instance = instance;
        }

        @Override
        public synchronized T get() {
            return instance;
        }
    }
}
