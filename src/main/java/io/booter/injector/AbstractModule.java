package io.booter.injector;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

import io.booter.injector.core.Key;

public abstract class AbstractModule implements Module {
    private Injector injector;

    @Override
    public void configure(Injector injector) {
        this.injector = injector;
        configure();
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

        public void to(Class<? extends T> implementation) {
            injector.bind(key, implementation, true);
        }

        public void toInstance(T instance) {
            injector.bind(key, instance, true);
        }

        public void toSupplier(Supplier<? extends T> supplier) {
            injector.bind(key, supplier, true);
        }

        public void toSingleton(Class<? extends T> implementation) {
            toLazySingleton(implementation);
        }

        public void toLazySingleton(Class<? extends T> implementation) {
            injector.bindSingleton(key, implementation, false, true);
        }

        public void toEagerSingleton(Class<? extends T> implementation) {
            injector.bindSingleton(key, implementation, true, true);
        }
    }
}
