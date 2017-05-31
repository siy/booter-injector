package io.booter.injector.core.supplier;

import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.function.Supplier;

import io.booter.injector.core.exception.InjectorException;

public final class Suppliers {
    private Suppliers() {
    }

    public static <T> Supplier<T> lazy(Supplier<T> factory) {
        return markableReferenceLazy(factory);
    }

    public static <T> Supplier<T> lambdaLazy(final Supplier<T> factory) {
        return new Supplier<T>() {
            private final Supplier<T> defaultDelegate = this::initializer;
            private Supplier<T> delegate = defaultDelegate;

            private T initializer() {
                synchronized (defaultDelegate) {
                    if (delegate != defaultDelegate) {
                        return delegate.get();
                    }

                    final T instance = factory.get();
                    delegate = () -> instance;
                    return instance;
                }
            }

            public T get() {
                return delegate.get();
            }
        };
    }

    public static <T> Supplier<T> doubleCheckedLazy(final Supplier<T> factory) {
        return new Supplier<T>() {
            private volatile Supplier<T> delegate = null;

            public T get() {
                Supplier<T> tmp = delegate;

                if (delegate == null) {
                    synchronized (this) {
                        tmp = delegate;

                        if (tmp == null) {
                            T instance = factory.get();
                            delegate = () -> instance;

                            return instance;
                        }
                    }
                }

                return delegate.get();
            }
        };
    }

    public static <T> Supplier<T> markableReferenceLazy(final Supplier<T> factory) {
        return new Supplier<T>() {
            private final AtomicMarkableReference<T> reference = new AtomicMarkableReference<>(null, false);
            private Supplier<T> delegate;

            public T get() {
                if (!reference.isMarked()) {
                    T instance = factory.get();
                    return reference.compareAndSet(null, instance, false, true) ? instance : reference.getReference();
                }

                return reference.getReference();
            }
        };
    }

    public static <T> Supplier<T> singleton(final Supplier<T> factory, boolean eager) {
        if (eager) {
            T instance = factory.get();
            return () -> instance;
        }
        return lazy(factory);
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

    public static <T> Supplier<T> enhancingConstructor(Constructor<T> constructor, Supplier<?>[] parameters) {
        Supplier<T> initialFactory = constructor(constructor, parameters);

        return new Supplier<T>() {
            private static final int TRESHOLD = 3;

            private final AtomicInteger counter = new AtomicInteger();
            private final AtomicMarkableReference<Supplier<T>> reference = new AtomicMarkableReference<>(initialFactory, false);

            @Override
            public T get() {
                if (!reference.isMarked()) {
                    if (counter.incrementAndGet() < TRESHOLD) {
                        return reference.getReference().get();
                    }
                    Supplier<T> supplier = LambdaFactory.create(constructor, parameters);

                    reference.compareAndSet(null, supplier, false, true);
                }

                return reference.getReference().get();
            }
        };
    }
}