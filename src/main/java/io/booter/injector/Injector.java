package io.booter.injector;

import io.booter.injector.core.ScanningInjector;

import java.util.function.Supplier;

public interface Injector {
    <T> T get(Class<T> clazz);

    <T> T get(Key key);

    <T> Supplier<T> supplier(Class<T> clazz);

    <T> Supplier<T> supplier(Key key);

    Injector configure(Class<?>... configurators);

    static Injector create() {
        return new ScanningInjector();
    }

    static Injector create(Class<?>...classes) {
        return new ScanningInjector().configure(classes);
    }
}
