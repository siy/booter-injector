package io.booter.injector;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

import io.booter.injector.Injector;
import io.booter.injector.InjectorConfig;
import io.booter.injector.TypeToken;
import io.booter.injector.core.AnnotationFactory;
import io.booter.injector.core.Key;

public abstract class AbstractInjectorConfig implements InjectorConfig {
    private Injector injector;

    @Override
    public void configure(Injector injector) {
        this.injector = injector;
        configure();
    }

    protected abstract void configure();

    <T> Builder<T> bind(Class<T> clazz) {
        return new Builder<>(Key.of(clazz));
    }

    <T> Builder<T> bind(TypeToken<T> token) {
        return new Builder<>(Key.of(token));
    }

    public class Builder<T> {
        private Key key;

        public Builder(Key key) {
            this.key = key;
        }

        public Builder<T> annotatedWith(Class<? extends Annotation> annotation) {
            key = Key.of(key, annotation);
            return this;
        }

        void to(Class<T> implementation) {
            injector.bind(key, implementation, true);
        }

        void toInstance(T instance) {
            injector.bind(key, instance, true);
        }

        void toSupplier(Supplier<T> supplier) {
            injector.bind(key, supplier, true);
        }

        void toSingleton(Class<T> implementation) {
            injector.bindSingleton(key, implementation, false, true);
        }

        void toLazySingleton(Class<T> implementation) {
            injector.bindSingleton(key, implementation, false, true);
        }

        void toEagerSingleton(Class<T> implementation) {
            injector.bindSingleton(key, implementation, true, true);
        }
    }
}
