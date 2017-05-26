package io.booter.injector;

import java.util.function.Supplier;

import io.booter.injector.core.Key;
import io.booter.injector.core.LazyInjector;

public interface Injector {
    <T> T get(Class<T> clazz);

    <T> T get(Key key);

    <T> Supplier<T> supplier(Class<T> clazz);

    <T> Supplier<T> supplier(Key key);

    <T> Injector bind(Key key, Supplier<T> supplier, boolean throwIfExists);

    <T> Injector bind(Key key, Class<T> implementation, boolean throwIfExists);

    <T> Injector bindSingleton(Key key, Class<T> implementation, boolean eager, boolean throwIfExists);

    <T> Injector bind(Key key, T implementation, boolean throwIfExists);

    static Injector create() {
        return new LazyInjector();
    }
}
