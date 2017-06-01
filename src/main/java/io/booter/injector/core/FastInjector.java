package io.booter.injector.core;

import java.lang.reflect.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import io.booter.injector.Injector;
import io.booter.injector.Module;
import io.booter.injector.annotations.ConfiguredBy;
import io.booter.injector.annotations.ImplementedBy;
import io.booter.injector.annotations.Inject;
import io.booter.injector.annotations.Supplies;
import io.booter.injector.core.exception.InjectorException;
import io.booter.injector.core.supplier.DefaultSupplierFactory;

import static io.booter.injector.core.supplier.Suppliers.*;

public class FastInjector implements Injector {
    private final ConcurrentMap<Key, Supplier<?>> bindings = new ConcurrentHashMap<>();
    private final SupplierFactory factory;

    public FastInjector() {
        this(new DefaultSupplierFactory());
    }

    public FastInjector(SupplierFactory factory) {
        this.factory = factory;
        bindings.put(Key.of(Injector.class), () -> this);
    }

    @Override
    public <T> T get(Class<T> clazz) {
        return supplier(clazz).get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Key key) {
        return (T) supplier(key).get();
    }

    @Override
    public <T> Supplier<T> supplier(Class<T> clazz) {
        return supplier(Key.of(clazz));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Supplier<T> supplier(Key key) {
        return (Supplier<T>) bindings.computeIfAbsent(key, (k) -> factoryLazy(() -> collectBindings(key)));
    }

    @Override
    public <T> Injector bind(Key key, Supplier<T> supplier, boolean throwIfExists) {
        return checkForExistingBinding(key, throwIfExists, bindings.putIfAbsent(key, supplier));
    }

    @Override
    public <T> Injector bind(Key key, Class<T> implementation, boolean throwIfExists) {
        Supplier<?> supplier = supplier(Key.of(implementation));
        return checkForExistingBinding(key, throwIfExists, bindings.putIfAbsent(key, supplier));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Injector bindSingleton(Key key, Class<T> implementation, boolean eager, boolean throwIfExists) {
		Constructor<T> constructor = (Constructor<T>) locateConstructorAndConfigureInjector(Key.of(implementation));
        Supplier<T> supplier = factory.createSingleton(constructor, collectParameterSuppliers(constructor), eager);

        return checkForExistingBinding(key, throwIfExists, bindings.putIfAbsent(key, supplier));
    }

    @Override
    public <T> Injector bind(Key key, T implementation, boolean throwIfExists) {
        return checkForExistingBinding(key, throwIfExists, bindings.putIfAbsent(key, () -> implementation));
    }

    @Override
    public Injector configure(Class<?>... configurators) {
        for(Class<?> clazz : configurators) {
            if (clazz == null) {
                throw new InjectorException("Null class is passed to configure()");
            }

            configure(clazz);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    <T> Supplier<T> collectBindings(Key key) {
        Constructor<?> constructor = locateConstructorAndConfigureInjector(key);

        return (Supplier<T>) factory.create(constructor, collectParameterSuppliers(constructor));
    }

    private Constructor<?> locateConstructorAndConfigureInjector(Key key) {
        Constructor<?> constructor = locateConstructor(key);

        ConfiguredBy configuredBy = constructor.getDeclaringClass().getAnnotation(ConfiguredBy.class);

        if (configuredBy != null) {
            configure(configuredBy.value());
        }

        return constructor;
    }

    private <T> Injector checkForExistingBinding(Key key, boolean throwIfExists, Supplier<T> existing) {
        if (throwIfExists && existing != null) {
            throw new InjectorException("Binding for " + key + " already exists");
        }

        return this;
    }

    private void configure(Class<?> clazz) {
        Supplier<?> configSupplier = supplier(Key.of(clazz));

        for(Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Supplies.class)) {
                addMethodBinding(method, configSupplier);
            }
        }

        if (clazz.isAssignableFrom(Module.class)) {
            ((Module) configSupplier.get()).configure(this);
        }
    }

    private <T> void addMethodBinding(Method method, Supplier<T> instanceSupplier) {
        Supplier<?>[] invocationParameters = buildMethodCallParameters(method, instanceSupplier);

        bindings.computeIfAbsent(Key.of(method.getGenericReturnType(), method.getAnnotations()),
                                 (key) -> fastMethodConstructor(method, invocationParameters));
    }

    private Supplier<?>[] buildMethodCallParameters(Method method, Supplier<?> instanceSupplier) {
        Supplier<?>[] methodParameters = collectParameterSuppliers(method);
        Supplier<?>[] invocationParameters = new Supplier<?>[methodParameters.length + 1];

        System.arraycopy(methodParameters, 0, invocationParameters, 1, methodParameters.length);
        invocationParameters[0] = instanceSupplier;

        return invocationParameters;
    }

    private Supplier<?>[] collectParameterSuppliers(Executable executable) {
        int i = 0;
        Supplier<?>[] suppliers = new Supplier[executable.getParameterCount()];

        for(Parameter p : executable.getParameters()) {
            suppliers[i++] = lookupParameterSupplier(Key.of(p));
        }

        return suppliers;
    }

    private Supplier<?> lookupParameterSupplier(Key parameterKey) {
        Supplier<?> supplier = supplier(parameterKey);

        return parameterKey.isSupplier() ? () -> supplier : supplier;
    }

    static Constructor<?> locateConstructor(Key key) {
        Class<?> clazz = key.rawClass();

        if (clazz.isInterface()) {
            ImplementedBy implementationClass = clazz.getAnnotation(ImplementedBy.class);

            if (implementationClass == null) {
                throw new InjectorException("Unable to locate suitable implementation for " + key);
            }

            clazz = implementationClass.value();
        }

        Constructor<?> instanceConstructor = null;
        Constructor<?> defaultConstructor = null;
        Constructor<?> singleConstructor = null;
        boolean singleConstructorIsPending = true;

        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (!Modifier.isPublic(constructor.getModifiers())) {
                continue;
            }

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
                     + " has more than one constructor annotated with @Inject");
            }

            instanceConstructor = constructor;
        }

        Constructor<?> constructor = instanceConstructor == null ? (defaultConstructor == null ? singleConstructor
                                                                                               : defaultConstructor)
                                                                 : instanceConstructor;

        if (constructor == null) {
            throw new InjectorException("Unable to locate suitable constructor for " + key);
        }

        return constructor;
    }
}
