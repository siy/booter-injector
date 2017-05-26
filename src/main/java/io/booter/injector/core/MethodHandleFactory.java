package io.booter.injector.core;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public interface MethodHandleFactory {
    MethodHandle from(Method method);

    MethodHandle from(Constructor<?> constructor);
}
