package io.booter.injector.core.supplier;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;

import io.booter.injector.annotations.ComputationStyle;
import io.booter.injector.annotations.Singleton;
import io.booter.injector.core.SupplierFactory;

import static io.booter.injector.core.supplier.Suppliers.*;

public class DefaultSupplierFactory implements SupplierFactory {
    public DefaultSupplierFactory() {
    }

    @Override
    public <T> Supplier<T> create(Constructor<T> constructor, Supplier<?>[] parameters) {
        Supplier<T> factory = tryWrapWithPostConstruct(constructor,
                                                       enhancing(constructor(constructor, parameters),
                                                                 () -> fastConstructor(constructor, parameters)));

        return tryBuildSingleton(factory, constructor.getDeclaringClass());
    }

    @Override
    public <T> Supplier<T> createSingleton(Constructor<T> constructor, Supplier<?>[] parameters, boolean eager) {
        Supplier<T> factory = tryWrapWithPostConstruct(constructor,
                                                       constructor(constructor, parameters));

        return singleton(factory, eager);
    }

    private <T> Supplier<T> tryWrapWithPostConstruct(Constructor<T> constructor, Supplier<T> factory) {
        MethodHandle methodHandle = LambdaFactory.locateAnnotated(constructor.getDeclaringClass(), PostConstruct.class);

        if (methodHandle != null) {
            return new PostProcessingSupplier(factory, methodHandle, "Error while invoking @PostConstruct method");
        }
        return factory;
    }

    private <T> Supplier<T> tryBuildSingleton(Supplier<T> instanceSupplier, Class<?> clazz) {
        Singleton singleton = clazz.getAnnotation(Singleton.class);

        if (singleton == null) {
            return instanceSupplier;
        }

        return singleton(instanceSupplier, singleton.value() == ComputationStyle.EAGER);
    }
}
