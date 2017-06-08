package io.booter.injector.core;

import io.booter.injector.Binding;
import io.booter.injector.Key;

import java.util.function.Supplier;

public final class Bindings {
    private Bindings() {}

    public static Binding<Class<?>> of(Key key, Class<?> implementation, boolean singleton, boolean eager) {
        return new BindingImpl<>(key, implementation, false, singleton, eager);
    }

    public static<T> Binding<Supplier<T>> of(Key key, T instance) {
        return new BindingImpl<>(key, () -> instance, true, true, true);
    }

    public static<T> Binding<Supplier<T>> of(Key key, Supplier<T> instance) {
        return new BindingImpl<>(key, instance, true, true, true);
    }

    private static class BindingImpl<T> implements Binding<T> {
        private final Key key;
        private final T binding;
        private final boolean resolved;
        private final boolean singleton;
        private final boolean eager;

        public BindingImpl(Key key, T binding, boolean resolved, boolean singleton, boolean eager) {
            this.key = key;
            this.binding = binding;
            this.resolved = resolved;
            this.singleton = singleton;
            this.eager = eager;
        }

        @Override
        public Key key() {
            return key;
        }

        @Override
        public T binding() {
            return binding;
        }

        @Override
        public boolean isResolved() {
            return resolved;
        }

        @Override
        public boolean isSingleton() {
            return singleton;
        }

        @Override
        public boolean isEager() {
            return eager;
        }
    }
}
