package io.booter.injector.core.supplier;

import java.lang.reflect.Executable;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import io.booter.injector.core.exception.InjectorException;

final class Utils {
    private Utils() {}

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
