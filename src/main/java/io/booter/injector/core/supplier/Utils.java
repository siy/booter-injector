package io.booter.injector.core.supplier;

import io.booter.injector.Key;
import io.booter.injector.annotations.ImplementedBy;
import io.booter.injector.annotations.Inject;
import io.booter.injector.core.exception.InjectorException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class Utils {
    private Utils() {}

    public static Constructor<?> locateConstructor(Key key) {
        return locateConstructor(key.rawClass());
    }

    public static Constructor<?> locateConstructor(Class<?> clazz) {
        if (clazz.isInterface()) {
            ImplementedBy implementationClass = clazz.getAnnotation(ImplementedBy.class);

            if (implementationClass == null || implementationClass.value().isInterface()) {
                throw new InjectorException("Unable to locate suitable implementation for " + clazz);
            }

            clazz = implementationClass.value();
        }

        Constructor<?> instanceConstructor = null;
        Constructor<?> defaultConstructor = null;
        Constructor<?> singleConstructor = null;
        boolean singleConstructorIsPending = true;

        Constructor<?>[] constructors = clazz.getConstructors();

        if (constructors.length == 1) {
            return constructors[0];
        }

        for (Constructor<?> constructor : constructors) {
            if (!constructor.isAnnotationPresent(Inject.class)) {
                if (constructor.getParameterCount() == 0) {
                    defaultConstructor = constructor;
                    singleConstructorIsPending = false;
                    singleConstructor = null;
                } else {
                    if (singleConstructorIsPending) {
                        singleConstructor = constructor;
                        singleConstructorIsPending = false;
                    } else {
                        singleConstructor = null;
                    }
                }

                continue;
            }

            if (instanceConstructor != null) {
                throw new InjectorException("Class "
                     + clazz.getCanonicalName()
                     + " has more than one createConstructorSupplier annotated with @Inject");
            }

            instanceConstructor = constructor;
        }

        Constructor<?> constructor = instanceConstructor == null ? (defaultConstructor == null ? singleConstructor
                                                                                               : defaultConstructor)
                                                                 : instanceConstructor;

        if (constructor == null) {
            throw new InjectorException("Unable to locate suitable createConstructorSupplier for " + clazz);
        }

        return constructor;
    }

    public interface ThrowingSupplier<T> {
        T get() throws Throwable;
    }

    public static <T> T safeCall(ThrowingSupplier<T> factory, Object method) {
        try {
            return factory.get();
        } catch (Throwable  e) {
            throw new InjectorException("Error while invoking " + Objects.toString(method), e);
        }
    }

    public static void validateParameters(Executable method, Supplier<?>[] suppliers, int extraParameters) {
        validateNotNull(method, suppliers);

        if (suppliers.length < method.getParameterCount() + extraParameters) {
            throw new InjectorException("Passed number of parameters is incorrect for " + method);
        }
    }

    public static void validateParameters(Executable method, List<Supplier<?>> suppliers, int extraParameters) {
        validateNotNull(method, suppliers);

        if (suppliers.size() < method.getParameterCount() + extraParameters) {
            throw new InjectorException("Passed number of parameters is incorrect for " + method);
        }
    }

    public static<T> void validateNotNull(T value) {
        if (value == null) {
            throw new InjectorException("Null value passed as a parameter");
        }
    }

    public static<T1, T2> void validateNotNull(T1 value1, T2 value2) {
        if (value1 == null || value2 == null) {
            throw new InjectorException("Null value passed as a parameter");
        }
    }
}
