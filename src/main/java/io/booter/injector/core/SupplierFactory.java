package io.booter.injector.core;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.function.Supplier;

public interface SupplierFactory {
    <T> Supplier<T> create(Constructor<T> constructor, List<Supplier<?>> parameters);

    <T> Supplier<T> create(Constructor<T> constructor, Supplier<?>[] parameters);

    <T> Supplier<T> createSingleton(Constructor<T> constructor, List<Supplier<?>> parameters, boolean eager);

    <T> Supplier<T> createSingleton(Constructor<T> constructor, Supplier<?>[] parameters, boolean eager);
}
