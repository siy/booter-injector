package io.booter.injector.core.supplier;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import io.booter.injector.core.MethodHandleFactory;
import io.booter.injector.core.exception.InjectorException;
import io.booter.injector.core.supplier.MethodHandleInvoker;

public class MethodCallSupplier<T> extends MethodHandleInvoker implements Supplier<T> {
    private final Method method;

    public MethodCallSupplier(Method method, Supplier<?>[] parameters) {
        super(LambdaFactory.create(method), parameters);
        this.method = method;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T get() {
        try {
            return (T) invoke();
        } catch (Throwable throwable) {
            throw new InjectorException("Error while invoking " + method, throwable);
        }
    }
}
