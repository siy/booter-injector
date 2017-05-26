package io.booter.injector.core.supplier;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;

import io.booter.injector.core.MethodHandleFactory;
import io.booter.injector.core.exception.InjectorException;

//TODO: improve lines coverage
abstract class AbstractSupplier<T> extends MethodHandleInvoker implements Supplier<T> {
    private final Optional<MethodHandle> postConstruct;
    private final Constructor<T> constructor;

    AbstractSupplier(Constructor<T> constructor, Supplier<?>[] suppliers, MethodHandleFactory factory) {
        super(factory.from(constructor), suppliers);
        this.constructor = constructor;
        this.postConstruct = locatePostConstruct(constructor.getDeclaringClass());
    }

    @SuppressWarnings("unchecked")
    T createInstance() {
        try {
            T instance = (T) invoke();
            postConstruct.ifPresent(m -> invoke(m, instance));
            return instance;
        } catch (Throwable throwable) {
            throw new InjectorException("Error while instantiating class " + constructor, throwable);
        }
    }

    private void invoke(MethodHandle handle, T instance) {
        try {
            handle.invoke(instance);
        } catch (Throwable e) {
            throw new InjectorException("Error while invoking @PostConstruct for " + constructor, e);
        }
    }

    private static <T> Optional<MethodHandle> locatePostConstruct(Class<T> declaringClass) {
        Method[] methods = declaringClass.getDeclaredMethods();

        for (Method method: methods) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    //TODO: how to report this case?
                    continue;
                }

                try {
                    return Optional.of(MethodHandles.lookup().unreflect(method));
                } catch (IllegalAccessException e) {
                    throw new InjectorException("Unable to obtain method handle (@PostConstruct) for "
                                                + declaringClass.getCanonicalName(), e);
                }
            }
        }
        return Optional.empty();
    }
}
