package io.booter.injector.core.supplier;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

import io.booter.injector.core.MethodHandleFactory;

class EagerSingletonSupplier<T> extends AbstractSupplier<T> {
    private final T instance;

    public EagerSingletonSupplier(Constructor<T> instanceConstructor, Supplier<?>[] suppliers,
                                  MethodHandleFactory factory) {
        super(instanceConstructor, suppliers, factory);
        this.instance = createInstance();
    }

    @Override
    public T get() {
        return instance;
    }
}
