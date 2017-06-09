package io.booter.injector;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.booter.injector.core.Bindings;

public abstract class AbstractModule implements Module {
    private final List<Binding<?>> bindings = new ArrayList<>();

    @Override
    public List<Binding<?>> collectBindings() {
        configure();
        return bindings;
    }

    protected abstract void configure();

    protected <T> Builder<T> bind(Class<T> clazz) {
        return new Builder<>(Key.of(clazz));
    }

    protected <T> Builder<T> bind(TypeToken<T> token) {
        return new Builder<>(Key.of(token));
    }

    public class Builder<T> {
        private Key key;

        public Builder(Key key) {
            this.key = key;
        }

        public Builder<T> annotatedWith(Class<? extends Annotation> annotation) {
            key = key.with(annotation);
            return this;
        }

        public Builder<T> named(String name) {
            key = key.with(Naming.with(name));
            return this;
        }

        public void to(Class<? extends T> implementation) {
            bindings.add(Bindings.of(key, implementation, false, false));
        }

        public void toInstance(T instance) {
            bindings.add(Bindings.of(key, instance));
        }

        public void toSupplier(Supplier<? extends T> supplier) {
            bindings.add(Bindings.of(key, supplier));
        }

        public void toSingleton(Class<? extends T> implementation) {
            toLazySingleton(implementation);
        }

        public void toLazySingleton(Class<? extends T> implementation) {
            bindings.add(Bindings.of(key, implementation, true, false));
        }

        public void toEagerSingleton(Class<? extends T> implementation) {
            bindings.add(Bindings.of(key, implementation, true, true));
        }
    }
}
