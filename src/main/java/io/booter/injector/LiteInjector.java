package io.booter.injector;

import io.booter.injector.core.Key;

import java.util.function.Supplier;

public interface LiteInjector {
    <T> T get(Class<T> clazz);

    <T> T get(Key key);

    <T> Supplier<T> supplier(Class<T> clazz);

    <T> Supplier<T> supplier(Key key);
}
