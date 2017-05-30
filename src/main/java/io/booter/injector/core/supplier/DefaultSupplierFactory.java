package io.booter.injector.core.supplier;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;

import io.booter.injector.annotations.ComputationStyle;
import io.booter.injector.annotations.Singleton;
import io.booter.injector.core.SupplierFactory;
import io.booter.injector.core.exception.InjectorException;

public class DefaultSupplierFactory implements SupplierFactory {
    public DefaultSupplierFactory() {
    }

    @Override
    public <T> Supplier<T> create(Constructor<T> constructor, Supplier<?>[] parameters) {
        Supplier<T> instanceSupplier = tryWrapWithPostConstruct(constructor,
                                                                new LazyEnhancingSupplier(constructor, parameters));

        return tryBuildSingleton(instanceSupplier, constructor.getDeclaringClass());
    }

    @Override
    public <T> Supplier<T> createSingleton(Constructor<T> constructor, Supplier<?>[] parameters, boolean eager) {
        Supplier<T> instanceSupplier = tryWrapWithPostConstruct(constructor,
                                                                new LazyEnhancingSupplier(constructor, parameters));

        return buildSingletonSupplier(instanceSupplier, eager);
    }

    private <T> Supplier<T> tryWrapWithPostConstruct(Constructor<T> constructor, Supplier<T> instanceSupplier) {
        MethodHandle methodHandle = locatePostConstructMethod(constructor.getDeclaringClass());

        if (methodHandle != null) {
            return new PostConstructInvokingSupplier(instanceSupplier, methodHandle);
        }
        return instanceSupplier;
    }

    private <T> MethodHandle locatePostConstructMethod(Class<T> declaringClass) {
        for (Method method: declaringClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                return LambdaFactory.create(method);
            }
        }
        return null;
    }

    private <T> Supplier<T> tryBuildSingleton(Supplier<T> instanceSupplier, Class<?> clazz) {
        Singleton singleton = clazz.getAnnotation(Singleton.class);

        if (singleton == null) {
            return instanceSupplier;
        }

        return buildSingletonSupplier(instanceSupplier, singleton.value() == ComputationStyle.EAGER);
    }

    private <T> Supplier<T> buildSingletonSupplier(Supplier<T> instanceSupplier, boolean isEager) {
        if (isEager) {
            T instance = instanceSupplier.get();
            return () -> instance;
        } else {
            return new InternalLazySupplier<>(instanceSupplier);
        }
    }

    private static class InternalLazySupplier<T> implements Supplier<T> {
        private final Supplier<T> defaultSupplier = () -> lazyInstanceCreator();
        private volatile Supplier<T> instanceSupplier = defaultSupplier;
        private final Supplier<T> instanceCreator;

        private synchronized T lazyInstanceCreator() {
            if (instanceSupplier != defaultSupplier) {
                return instanceSupplier.get();
            }

            T instance = instanceCreator.get();
            instanceSupplier = () -> instance;
            return instance;
        }

        InternalLazySupplier(Supplier<T> instanceCreator) {
            this.instanceCreator = instanceCreator;
        }

        @Override
        public T get() {
            return instanceSupplier.get();
        }
    }

    private class ConstructingSupplier<T> implements Supplier<T> {
        protected final Constructor<T> constructor;
        protected final Supplier<?>[] parameters;

        private ConstructingSupplier(Constructor<T> constructor, Supplier<?>[] parameters) {
            this.constructor = constructor;
            this.parameters = parameters;
        }

        private T createInstance() {
            try {
                Object[] values = new Object[constructor.getParameterCount()];
                for (int i = 0; i < values.length; i++) {
                    values[i] = parameters[i].get();
                }

                return constructor.newInstance(values);
            } catch (Exception e) {
                throw new InjectorException("Unable to create instance. Calling " + constructor);
            }
        }
        @Override
        public T get() {
            return createInstance();
        }
    }

    private class LazyEnhancingSupplier<T> extends ConstructingSupplier<T> {
        private static final int TRESHOLD = 3;

        private final AtomicInteger counter = new AtomicInteger();
        private final Supplier<T> defaultSupplier = () -> create();
        private volatile Supplier<T> supplier = defaultSupplier;

        private T create() {
            if (counter.incrementAndGet() < TRESHOLD) {
                return super.get();
            }

            synchronized (defaultSupplier) {
                if (supplier == defaultSupplier) {
                    supplier = LambdaFactory.create(constructor, parameters);
                }
            }
            return supplier.get();
        }

        public LazyEnhancingSupplier(Constructor<T> constructor, Supplier<?>[] parameters) {
            super(constructor, parameters);
        }

        @Override
        public T get() {
            return supplier.get();
        }
    }
}
