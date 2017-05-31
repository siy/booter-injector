package io.booter.injector.core.supplier;

import java.lang.invoke.MethodHandle;
import java.util.function.Supplier;

import io.booter.injector.core.exception.InjectorException;

public class PostProcessingSupplier<T> implements Supplier<T> {

    private final Supplier<T> instanceSupplier;
    private final MethodHandle methodHandle;
    private final String message;

    PostProcessingSupplier(Supplier<T> instanceSupplier, MethodHandle methodHandle, String message) {
        this.instanceSupplier = instanceSupplier;
        this.methodHandle = methodHandle;
        this.message = message;
    }

    @Override
    public T get() {
        T instance = instanceSupplier.get();

        try {
            methodHandle.invoke(instance);
        } catch (Throwable throwable) {
            throw new InjectorException(message, throwable);
        }

        return instance;
    }
}
