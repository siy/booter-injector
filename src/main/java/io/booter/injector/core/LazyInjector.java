package io.booter.injector.core;

import java.lang.reflect.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import io.booter.injector.Injector;
import io.booter.injector.InjectorConfig;
import io.booter.injector.annotations.ConfiguredBy;
import io.booter.injector.annotations.ImplementedBy;
import io.booter.injector.annotations.Inject;
import io.booter.injector.annotations.Supplies;
import io.booter.injector.core.exception.InjectorException;
import io.booter.injector.core.supplier.DefaultSupplierFactory;
import io.booter.injector.core.supplier.MethodCallSupplier;

public class LazyInjector implements Injector {
    private final ConcurrentMap<Key, Supplier<?>> bindings = new ConcurrentHashMap<>();
    private final SupplierFactory factory;

    public LazyInjector() {
        this(new DefaultSupplierFactory());
    }

    public LazyInjector(SupplierFactory factory) {
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
        return (Supplier<T>) bindings.computeIfAbsent(key,
                                                      (k) -> new PlaceholderSupplier<>(this, k));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Injector bind(Key key, Supplier<T> supplier, boolean throwIfExists) {
        return verifyAdded(key, throwIfExists, bindings.putIfAbsent(key, supplier));
    }

    @Override
    public <T> Injector bind(Key key, Class<T> implementation, boolean throwIfExists) {
        Supplier<?> supplier = supplier(Key.of(implementation));
        return verifyAdded(key, throwIfExists, bindings.putIfAbsent(key, supplier));
    }

    @Override
    public <T> Injector bindSingleton(Key key, Class<T> implementation, boolean eager, boolean throwIfExists) {
        Constructor<T> constructor = (Constructor<T>) locateConstructorAndConfigure(Key.of(implementation));
        Supplier<T> supplier = factory.createSingleton(constructor, collectParameterSuppliers(constructor), eager);

        return verifyAdded(key, throwIfExists, bindings.putIfAbsent(key, supplier));
    }

    @Override
    public <T> Injector bind(Key key, T implementation, boolean throwIfExists) {
        return verifyAdded(key, throwIfExists, bindings.putIfAbsent(key, () -> implementation));
    }

    @SuppressWarnings("unchecked")
    <T> Supplier<T> collectBindings(Key key) {
        Constructor<?> constructor = locateConstructorAndConfigure(key);

        return (Supplier<T>) factory.create(constructor, collectParameterSuppliers(constructor));
    }

    private Constructor<?> locateConstructorAndConfigure(Key key) {
        Constructor<?> constructor = locateConstructor(key);

        ConfiguredBy configuredBy = constructor.getDeclaringClass().getAnnotation(ConfiguredBy.class);

        if (configuredBy != null) {
            updateConfiguration(configuredBy);
        }

        return constructor;
    }

    private <T> Injector verifyAdded(Key key, boolean throwIfExists, Supplier<T> existing) {
        if (throwIfExists && existing != null) {
            throw new InjectorException("Binding for " + key + " already exists");
        }

        return this;
    }

    private void updateConfiguration(ConfiguredBy configuredBy) {
        Class<?> clazz = configuredBy.value();

        Supplier<?> configSupplier = supplier(Key.of(clazz));

        for(Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Supplies.class)) {
                addBinding(method, configSupplier);
            }
        }

        if (clazz.isAssignableFrom(InjectorConfig.class)) {
            ((InjectorConfig) configSupplier.get()).configure(this);
        }
    }

    private void addBinding(Method method, Supplier<?> instanceSupplier) {
        Supplier<?>[] invocationParameters = buildMethodCallParameters(method, instanceSupplier);

        bindings.computeIfAbsent(Key.of(method.getGenericReturnType()),
                                 (key) -> new MethodCallSupplier<>(method, invocationParameters));
    }

    private Supplier<?>[] buildMethodCallParameters(Method method, Supplier<?> instanceSupplier) {
        Supplier<?>[] methodParameters = collectParameterSuppliers(method);
        Supplier<?>[] invocationParameters = new Supplier<?>[methodParameters.length + 1];

        System.arraycopy(methodParameters, 0, invocationParameters, 1, methodParameters.length);
        invocationParameters[0] = instanceSupplier;

        return invocationParameters;
    }

    private Supplier<?>[] collectParameterSuppliers(Executable instanceConstructor) {
        Parameter[] parameters = instanceConstructor.getParameters();
        Supplier<?>[] suppliers = new Supplier<?>[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Key parameterKey = calculateParameterKey(parameters[i]);
            suppliers[i] = lookupSupplier(parameterKey, isSupplier(parameters[i]));
        }

        return suppliers;
    }

    private Supplier<?> lookupSupplier(Key parameterKey, boolean wrapSupplier) {
        Supplier<?> supplier = supplier(parameterKey);
        return wrapSupplier ? () -> supplier : supplier;
    }

    private static boolean isSupplier(Parameter parameter) {
        //Note that this check limits parameter type exactly to Supplier class.
        //Opposite order would allow all derived classes to be accepted which is not feasible.
        return parameter.getType().isAssignableFrom(Supplier.class);
    }

    private static Key calculateParameterKey(Parameter parameter) {
        if (!isSupplier(parameter)) {
            return Key.of(parameter);
        }

        Type type = parameter.getParameterizedType();

        if (type instanceof ParameterizedType) {
            Type[] args = ((ParameterizedType) type).getActualTypeArguments();

            if (args.length > 0 && args[0] instanceof Class) {
                return Key.of(args[0]);
            }
        }

        throw new InjectorException("Unable to determine parameter type for " + parameter);
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

        //Note that we intentionally loop through all constructors even when required one is found.
        //This is necessary for checking of multiple constructors annotated with @Inject
        for (Constructor<?> constructor : clazz.getConstructors()) {
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
