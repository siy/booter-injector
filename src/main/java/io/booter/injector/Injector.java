package io.booter.injector;

import java.util.function.Supplier;

import io.booter.injector.core.Key;
import io.booter.injector.core.FastInjector;

public interface Injector {
    <T> T get(Class<T> clazz);

    <T> T get(Key key);

    <T> Supplier<T> supplier(Class<T> clazz);

    <T> Supplier<T> supplier(Key key);

    <T> Injector bind(Key key, Supplier<T> supplier, boolean throwIfExists);

    <T> Injector bind(Key key, Class<T> implementation, boolean throwIfExists);

    <T> Injector bindSingleton(Key key, Class<T> implementation, boolean eager, boolean throwIfExists);

    <T> Injector bind(Key key, T implementation, boolean throwIfExists);

    Injector configure(Class<?>... configurators);

    static Injector create() {
        return new FastInjector();
    }

    static Injector create(Class<?>...classes) {
        return new FastInjector().configure(classes);
    }
}
