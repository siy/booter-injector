package io.booter.injector.core.supplier;

import java.lang.invoke.MethodHandle;
import java.util.function.Supplier;

import io.booter.injector.core.exception.InjectorException;

public class PostConstructInvokingSupplier<T> implements Supplier<T> {

    private final Supplier<T> instanceSupplier;
    private final MethodHandle methodHandle;

    PostConstructInvokingSupplier(Supplier<T> instanceSupplier, MethodHandle methodHandle) {
        this.instanceSupplier = instanceSupplier;
        this.methodHandle = methodHandle;
    }

    @Override
    public T get() {
        T instance = instanceSupplier.get();

        try {
            methodHandle.invoke(instance);
        } catch (Throwable throwable) {
            throw new InjectorException("Error while invoking @PostConstruct method", throwable);
        }

        return instance;
    }
}
