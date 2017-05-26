package io.booter.injector.core.supplier;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

import io.booter.injector.core.MethodHandleFactory;

class FactorySupplier<T> //extends AbstractSupplier<T>
implements Supplier<T> {
    private final Supplier<T> lambda;

    FactorySupplier(Constructor<T> instanceConstructor, Supplier<?>[] suppliers,
                    MethodHandleFactory factory) {
        //super(instanceConstructor, suppliers, factory);
        this.lambda = LambdaFactory.create(instanceConstructor, suppliers);
    }

    @Override
    public T get() {
//        return createInstance();
        return lambda.get();
    }
}
