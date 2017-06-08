package io.booter.injector.core.supplier;

import io.booter.injector.annotations.ComputationStyle;
import io.booter.injector.annotations.Singleton;

import javax.annotation.PostConstruct;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;

import static io.booter.injector.core.supplier.Suppliers.enhancing;
import static io.booter.injector.core.supplier.Suppliers.singleton;
import static io.booter.injector.core.supplier.Utils.*;

public final class SupplierFactory {
    private SupplierFactory() {}

    public static <T> Supplier<T> createInstanceSupplier(Constructor<T> constructor, List<Supplier<?>> parameters) {
        validateParameters(constructor, parameters, 0);

        Supplier<T> factory = tryWrapWithPostConstruct(constructor,
                                                       enhancing(createConstructorSupplier(constructor, parameters),
                                                                 () -> fastConstructor(constructor, parameters)));

        return tryBuildSingleton(factory, constructor.getDeclaringClass());
    }

    public static <T> Supplier<T> createSingletonSupplier(Constructor<T> constructor, List<Supplier<?>> parameters, boolean eager) {
        validateParameters(constructor, parameters, 0);

        Supplier<T> factory = tryWrapWithPostConstruct(constructor,
                                                       createConstructorSupplier(constructor, parameters));

        return singleton(factory, eager);
    }

    private static <T> Supplier<T> tryWrapWithPostConstruct(Constructor<T> constructor, Supplier<T> factory) {
        MethodHandle methodHandle = LambdaFactory.locateAnnotated(constructor.getDeclaringClass(), PostConstruct.class);

        if (methodHandle == null) {
            return factory;
        }

        return () -> {
            T instance = factory.get();

            safeCall(() -> methodHandle.invoke(instance), () -> "@PostConstruct method for " + constructor.getDeclaringClass());
            return instance;
        };
    }

    private static <T> Supplier<T> tryBuildSingleton(Supplier<T> instanceSupplier, Class<?> clazz) {
        Singleton singleton = clazz.getAnnotation(Singleton.class);

        if (singleton == null) {
            return instanceSupplier;
        }

        return singleton(instanceSupplier, singleton.value() == ComputationStyle.EAGER);
    }

    @SuppressWarnings("unchecked")
    public static <T> Supplier<T> createMethodSupplier(Method method, List<Supplier<?>> parameters) {
        validateParameters(method, parameters, 1);
        Supplier<?>[] suppliers = parameters.toArray(new Supplier[parameters.size()]);

        return () -> safeCall(mapMethodParameters(method, suppliers), method);
    }

    @SuppressWarnings("unchecked")
    private static <T> ThrowingSupplier<T> mapMethodParameters(Method method, Supplier<?>[] suppliers) {
        switch (method.getParameterCount()) {
            case  0: return () -> (T) method.invoke(suppliers[0].get());
            case  1: return () -> (T) method.invoke(suppliers[0].get(), suppliers[1].get());
            case  2: return () -> (T) method.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get());
            case  3: return () -> (T) method.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(), suppliers[3].get());
            case  4: return () -> (T) method.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(), suppliers[3].get(), suppliers[4].get());
            case  5: return () -> (T) method.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(), suppliers[3].get(), suppliers[4].get(), suppliers[5].get());
            case  6: return () -> (T) method.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(), suppliers[3].get(), suppliers[4].get(), suppliers[5].get(), suppliers[6].get());
            case  7: return () -> (T) method.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(), suppliers[3].get(), suppliers[4].get(), suppliers[5].get(), suppliers[6].get(), suppliers[7].get());
            case  8: return () -> (T) method.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(), suppliers[3].get(), suppliers[4].get(), suppliers[5].get(), suppliers[6].get(), suppliers[7].get(), suppliers[8].get());
            case  9: return () -> (T) method.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(), suppliers[3].get(), suppliers[4].get(), suppliers[5].get(), suppliers[6].get(), suppliers[7].get(), suppliers[8].get(), suppliers[9].get());
            case 10: return () -> (T) method.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(), suppliers[3].get(), suppliers[4].get(), suppliers[5].get(), suppliers[6].get(), suppliers[7].get(), suppliers[8].get(), suppliers[9].get(), suppliers[10].get());
            default:
                //Should not happen, limits are already checked
                return null;
        }
    }

    public static <T> Supplier<T> createConstructorSupplier(Constructor<T> constructor, List<Supplier<?>> parameters) {
        validateParameters(constructor, parameters, 0);
        Supplier<?>[] suppliers = parameters.toArray(new Supplier[parameters.size()]);

        return () -> safeCall(mapConstructorParameters(constructor, suppliers), constructor);
    }

    private static <T> ThrowingSupplier<T> mapConstructorParameters(Constructor<T> constructor, Supplier<?>[] suppliers) {
        switch (constructor.getParameterCount()) {
            case  0: return () -> constructor.newInstance();
            case  1: return () -> constructor.newInstance(suppliers[0].get());
            case  2: return () -> constructor.newInstance(suppliers[0].get(), suppliers[1].get());
            case  3: return () -> constructor.newInstance(suppliers[0].get(), suppliers[1].get(), suppliers[2].get());
            case  4: return () -> constructor.newInstance(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(), suppliers[3].get());
            case  5: return () -> constructor.newInstance(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(), suppliers[3].get(), suppliers[4].get());
            case  6: return () -> constructor.newInstance(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(), suppliers[3].get(), suppliers[4].get(), suppliers[5].get());
            case  7: return () -> constructor.newInstance(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(), suppliers[3].get(), suppliers[4].get(), suppliers[5].get(), suppliers[6].get());
            case  8: return () -> constructor.newInstance(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(), suppliers[3].get(), suppliers[4].get(), suppliers[5].get(), suppliers[6].get(), suppliers[7].get());
            case  9: return () -> constructor.newInstance(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(), suppliers[3].get(), suppliers[4].get(), suppliers[5].get(), suppliers[6].get(), suppliers[7].get(), suppliers[8].get());
            case 10: return () -> constructor.newInstance(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(), suppliers[3].get(), suppliers[4].get(), suppliers[5].get(), suppliers[6].get(), suppliers[7].get(), suppliers[8].get(), suppliers[9].get());
            default:
                //Should not happen, limits are already checked
                return null;
        }
    }

    public static <T> Supplier<T> fastConstructor(Constructor<T> constructor, List<Supplier<?>> parameters) {
        return LambdaFactory.create(constructor, parameters.toArray(new Supplier[parameters.size()]));
    }

    public static <T> Supplier<T> fastMethodConstructor(Method method, List<Supplier<?>> parameters) {
        return LambdaFactory.create(method, parameters.toArray(new Supplier[parameters.size()]));
    }
}
