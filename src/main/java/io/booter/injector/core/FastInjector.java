package io.booter.injector.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import io.booter.injector.core.supplier.SupplierFactory;

import static io.booter.injector.core.supplier.SupplierFactory.*;
import static io.booter.injector.core.supplier.Suppliers.*;

public class FastInjector implements Injector {
    public static final int INITIAL_CAPACITY = 10;
    private final ConcurrentMap<Key, Supplier<?>> bindings = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, Class<?>> modules = new ConcurrentHashMap<>();

    public FastInjector() {
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
        return supplier(key, new ConcurrentHashMap<>());
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
        Key implKey = Key.of(implementation);
        Map<Key, Key> dependencies = new HashMap<>();
        dependencies.put(key, key);

        Constructor<T> constructor = (Constructor<T>) locateConstructorAndConfigureInjector(implKey);

        Supplier<T> supplier = SupplierFactory.createSingletonSupplier(constructor,
                                                       buildConstructorCallParameters(constructor, dependencies),
                                                       eager);

        return checkForExistingBinding(key, throwIfExists, bindings.putIfAbsent(key, supplier));
    }

    @Override
    public <T> Injector bind(Key key, T implementation, boolean throwIfExists) {
        return checkForExistingBinding(key, throwIfExists, bindings.putIfAbsent(key, () -> implementation));
    }

    @Override
    public Injector configure(Class<?>... configurators) {
        if (configurators == null) {
            throw new InjectorException("Null class is passed to configure()");
        }

        for(Class<?> clazz : configurators) {
            if (clazz == null) {
                throw new InjectorException("Null class is passed to configure()");
            }

            configure(clazz);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    <T> Supplier<T> collectBindings(Key key, Map<Key, Key> dependencies) {
        Constructor<?> constructor = locateConstructorAndConfigureInjector(key);

        return (Supplier<T>) SupplierFactory.createInstanceSupplier(constructor,
                                            buildConstructorCallParameters(constructor, new HashMap<>(dependencies)));
    }

    @SuppressWarnings("unchecked")
    private <T> Supplier<T> supplier(Key key, Map<Key, Key> dependencies) {
        if (!key.isSupplier()) {
            if (dependencies.containsKey(key)) {
                throw new InjectorException("Cycle detected for " + key);
            }
            dependencies.put(key, key);
        }

        return (Supplier<T>) bindings.computeIfAbsent(key, (k) -> factoryLazy(() -> collectBindings(key, dependencies)));
    }

    private Constructor<?> locateConstructorAndConfigureInjector(Key key) {
        Constructor<?> constructor = locateConstructor(key);
        ConfiguredBy configuredBy = constructor.getDeclaringClass().getAnnotation(ConfiguredBy.class);

        if (configuredBy != null) {
            modules.computeIfAbsent(configuredBy.value(), (clazz) -> configure(clazz));
        }

        return constructor;
    }

    private <T> Injector checkForExistingBinding(Key key, boolean throwIfExists, Supplier<T> existing) {
        if (throwIfExists && existing != null) {
            throw new InjectorException("Binding for " + key + " already exists");
        }

        return this;
    }

    private Class<?> configure(Class<?> clazz) {
        Supplier<?> configSupplier = supplier(Key.of(clazz));

        for(Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Supplies.class)) {
                addMethodBinding(method, configSupplier);
            }
        }

        if (Module.class.isAssignableFrom(clazz)) {
            ((Module) configSupplier.get()).configure(this);
        }
        
        return clazz;
    }

    private <T> void addMethodBinding(Method method, Supplier<T> instanceSupplier) {
        HashMap<Key, Key> dependencies = new HashMap<>();
        Key key = Key.of(method.getGenericReturnType(), method.getAnnotations());

        dependencies.put(key, key);

        List<Supplier<?>> parameters = buildMethodCallParameters(method, instanceSupplier, dependencies);

        bindings.computeIfAbsent(key, (k) -> enhancing(createMethodSupplier(method, parameters),
                                                       () -> fastMethodConstructor(method, parameters)));
    }

    private List<Supplier<?>> buildMethodCallParameters(Method method, Supplier<?> instanceSupplier,
                                                        HashMap<Key, Key> dependencies) {
        List<Supplier<?>> suppliers = new ArrayList<>(INITIAL_CAPACITY);
        suppliers.add(instanceSupplier);
        collectParameterSuppliers(method, dependencies, suppliers);

        return suppliers;
    }

    private List<Supplier<?>> buildConstructorCallParameters(Executable executable,
                                                             Map<Key, Key> dependencies) {
        return collectParameterSuppliers(executable, dependencies, new ArrayList<>(INITIAL_CAPACITY));
    }

    private List<Supplier<?>> collectParameterSuppliers(Executable executable,
                                                        Map<Key, Key> dependencies,
                                                        List<Supplier<?>> suppliers) {
        for(Parameter p : executable.getParameters()) {
            Key key = Key.of(p);
            Supplier<?> supplier = supplier(key, dependencies);
            suppliers.add(key.isSupplier() ? () -> supplier : supplier);
        }
        return suppliers;
    }

    static Constructor<?> locateConstructor(Key key) {
        Class<?> clazz = key.rawClass();

        if (clazz.isInterface()) {
            ImplementedBy implementationClass = clazz.getAnnotation(ImplementedBy.class);

            if (implementationClass == null || implementationClass.value().isInterface()) {
                throw new InjectorException("Unable to locate suitable implementation for " + key);
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
            throw new InjectorException("Unable to locate suitable createConstructorSupplier for " + key);
        }

        return constructor;
    }
}
