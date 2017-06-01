package io.booter.injector.core.supplier;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.function.Supplier;

import io.booter.injector.core.exception.InjectorException;

public final class Suppliers {
    private Suppliers() {
    }

    public static <T> Supplier<T> factoryLazy(final Supplier<Supplier<T>> factory) {
        return new Supplier<T>() {
            private final Supplier<T> defaultDelegate = () -> init();
            private final AtomicBoolean marker = new AtomicBoolean();
            private Supplier<T> delegate = defaultDelegate;

            private T init() {
                if (marker.compareAndSet(false, true)) {
                    delegate = factory.get();
                } else {
                    while (delegate == defaultDelegate) {
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
        return factoryLazy(() -> {T instance = factory.get(); return () -> instance;});
    }

    public static <T> Supplier<T> singleton(final Supplier<T> factory, boolean eager) {
        if (eager) {
            T instance = factory.get();
            return () -> instance;
        }
        return lazy(factory);
    }

    public static <T> Supplier<T> enhancing(final Supplier<T> initial, Supplier<Supplier<T>> enhanced) {
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

    public static <T> Supplier<T> instantiator(Method method, Supplier<?>[] parameters) {
        if (method == null || parameters == null || parameters.length < (method.getParameterCount() + 1)) {
            throw new InjectorException("Invalid parameters: method "
                                        + Objects.toString(method)
                                        + ", parameters "
                                        + Objects.toString(parameters));
        }

        return () -> {
            try {
                Object[] values = new Object[method.getParameterCount() - 1];
                for (int i = 0; i < values.length - 1; i++) {
                    values[i] = parameters[i + 1].get();
                }

                return (T) method.invoke(parameters[0].get(), values);
            } catch (Exception e) {
                throw new InjectorException("Unable to create instance. Calling " + method);
            }
        };
    }

    public static <T> Supplier<T> constructor(Constructor<T> constructor, Supplier<?>[] parameters) {
        return () -> {
            try {
                Object[] values = new Object[constructor.getParameterCount()];
                for (int i = 0; i < values.length; i++) {
                    values[i] = parameters[i].get();
                }

                return constructor.newInstance(values);
            } catch (Exception e) {
                throw new InjectorException("Unable to create instance. Calling " + constructor);
            }
        };
    }

    public static <T> Supplier<T> fastConstructor(Constructor<T> constructor, Supplier<?>[] parameters) {
        return LambdaFactory.create(constructor, parameters);
    }

    public static <T> Supplier<T> fastMethodConstructor(Method method, Supplier<?>[] parameters) {
        return LambdaFactory.create(method, parameters);
    }
}