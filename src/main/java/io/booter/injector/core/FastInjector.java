package io.booter.injector.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.booter.injector.Injector;
import io.booter.injector.core.exception.InjectorException;
import io.booter.injector.core.supplier.LambdaFactory;

import static io.booter.injector.core.LazyInjector.locateConstructor;

public class FastInjector implements Injector {
    private final ConcurrentMap<Key, Supplier<?>> bindings = new ConcurrentHashMap<>();

    public FastInjector() {
        bindings.put(Key.of(Injector.class), () -> this);
    }

    @Override
    public <T> T get(Class<T> clazz) {
        return get(Key.of(clazz));
    }

    @Override
    public <T> T get(Key key) {
        return (T) supplier(key).get();
    }

    @Override
    public <T> Supplier<T> supplier(Class<T> clazz) {
        return supplier(Key.of(clazz));
    }

    @Override
    public <T> Supplier<T> supplier(Key key) {
        return (Supplier<T>) bindings.computeIfAbsent(key, (k) -> collectBindings(key));
    }

    private Supplier<?> collectBindings(Key key) {
        Constructor<?> constructor = locateConstructor(key);

        Supplier[] list = Arrays.stream(constructor.getParameters())
                                .map(p -> Key.of(p))
                                .map(k -> ForkJoinPool.commonPool().submit(() -> lookupSupplier(k)))
                                .map(f -> {
                                    try {
                                        return f.get();
                                    } catch (Exception e) {
                                        throw new InjectorException("Unable to build supplier for " + key);
                                    }
                                })
                                .toArray(Supplier[]::new);

        return LambdaFactory.create(constructor, list);
    }

    private Supplier<?> lookupSupplier(Key k) {
        return k.isSupplier() ? () -> supplier(k) : supplier(k);
    }

    @Override
    public <T> Injector bind(Key key, Supplier<T> supplier, boolean throwIfExists) {
        return null;
    }

    @Override
    public <T> Injector bind(Key key, Class<T> implementation, boolean throwIfExists) {
        return null;
    }

    @Override
    public <T> Injector bindSingleton(Key key, Class<T> implementation, boolean eager, boolean throwIfExists) {
        return null;
    }

    @Override
    public <T> Injector bind(Key key, T implementation, boolean throwIfExists) {
        return null;
    }
}
