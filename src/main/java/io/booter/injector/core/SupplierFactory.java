package io.booter.injector.core;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

public interface SupplierFactory {
    <T> Supplier<T> create(Constructor<T> constructor, Supplier<?>[] parameters);

    <T> Supplier<T> createSingleton(Constructor<T> constructor, Supplier<?>[] parameters, boolean eager);
}
