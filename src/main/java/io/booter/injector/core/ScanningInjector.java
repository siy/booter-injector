package io.booter.injector.core;

import io.booter.injector.Injector;
import io.booter.injector.annotations.ConfiguredBy;
import io.booter.injector.annotations.ImplementedBy;
import io.booter.injector.core.exception.InjectorException;
import io.booter.injector.core.supplier.Utils;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.booter.injector.core.supplier.SupplierFactory.createInstanceSupplier;
import static io.booter.injector.core.supplier.Suppliers.factoryLazy;
import static io.booter.injector.core.supplier.Suppliers.singleton;

public class ScanningInjector implements Injector {
    private final ConcurrentMap<Key, Supplier<?>> bindings = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, Class<?>> modules = new ConcurrentHashMap<>();

    public ScanningInjector() {
        bindings.put(Key.of(Injector.class), () -> this);
    }

    @Override
    public <T> T get(Class<T> clazz) {
        return supplier(clazz).get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Key key) {
        return (T) supplier(key).get();
    }

    @Override
    public <T> Supplier<T> supplier(Class<T> clazz) {
        return supplier(Key.of(clazz));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Supplier<T> supplier(Key key) {
        Supplier<T> supplier = (Supplier<T>) bindings.get(key);

        if (supplier != null) {
            return supplier;
        }

        synchronized (bindings) {
            supplier = (Supplier<T>) bindings.get(key);

            if (supplier != null) {
                return supplier;
            }

            buildTree(key, new LinkedHashMap<>()).values().forEach(this::bindNode);

            return (Supplier<T>) bindings.get(key);
        }
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

        buildTree(implKey, new LinkedHashMap<>()).values().forEach(this::bindNode);

        return checkForExistingBinding(key, throwIfExists,
                bindings.putIfAbsent(key,
                        singleton(this.<T>supplier(implKey), eager)));
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
    private <T> Injector checkForExistingBinding(Key key, boolean throwIfExists, Supplier<T> existing) {
        if (throwIfExists && existing != null) {
            throw new InjectorException("Binding for " + key + " already exists");
        }

        return this;
    }

    private Constructor<?> locateConstructorAndConfigureInjector(Key key) {
        Constructor<?> constructor = Utils.locateConstructor(key);
        ConfiguredBy configuredBy = constructor.getDeclaringClass().getAnnotation(ConfiguredBy.class);

        if (configuredBy != null) {
            modules.computeIfAbsent(configuredBy.value(), (clazz) -> configure(clazz));
        }

        return constructor;
    }

    private Class<?> configure(Class<?> clazz) {
        //TODO: finish it!!!
//        Supplier<?> configSupplier = supplier(Key.of(clazz));
//
//        for(Method method : clazz.getDeclaredMethods()) {
//            if (method.isAnnotationPresent(Supplies.class)) {
//                addMethodBinding(method, configSupplier);
//            }
//        }
//
//        if (Module.class.isAssignableFrom(clazz)) {
//            ((Module) configSupplier.get()).configure(this);
//        }

        return clazz;
    }

    private void bindNode(Node node) {
        bindings.computeIfAbsent(node.key(), (k) -> factoryLazy(() -> buildSupplier(node)));
    }

    private Supplier<?> buildSupplier(Node node) {
        return createInstanceSupplier(node.constructor(),
                                      collectSuppliers(node.dependencies()));
    }

    private List<Supplier<?>> collectSuppliers(List<Key> dependencies) {
        return dependencies.stream()
                .map(this::wrapSupplier)
                .collect(Collectors.toList());
    }

    private Supplier<?> wrapSupplier(Key key) {
        return key.isSupplier() ? () -> bindings.get(key) : bindings.get(key);
    }

    private Map<Key, Node> buildTree(Key key, Map<Key, Node> nodes) {
        Node existing = nodes.get(key);
        if (existing != null) {
//            if (existing.key().isSupplier() || key.isSupplier()) {
//                if (key.isSupplier() && !existing.key.isSupplier()) {
//
//                }
                return nodes;
//            }
//
//            throw new InjectorException("Cycle is detected for " + key);
        }

        Node node = buildNode(key);
        nodes.put(key, node);

        node.dependencies().forEach((k) -> buildTree(k, nodes));

        return nodes;
    }

//    private Map<Key, Node> buildMethodTree(Method method) {
//
//        Node node = buildNode(key);
//        nodes.put(key, node);
//
//        node.dependencies().forEach((k) -> buildTree(k, nodes));
//
//        return nodes;
//    }

    private Node buildNode(Key key) {
        Constructor<?> constructor = null;

        if(key.rawClass().isInterface()) {
            ImplementedBy implementedBy = key.rawClass().getAnnotation(ImplementedBy.class);

            if (implementedBy != null && !implementedBy.value().isInterface()) {
                constructor = locateConstructorAndConfigureInjector(Key.of(implementedBy.value()));
            } else {
                throw new InjectorException("Unable to find suitable constructor for " + key);
            }
        } else {
            constructor = locateConstructorAndConfigureInjector(key);
        }

        List<Key> dependencies = Arrays.stream(constructor.getParameters()).map(Key::of).collect(Collectors.toList());
        return new Node(key, dependencies, constructor);
    }

    public static class Node {
        private final Key key;
        private final List<Key> dependencies;
        private final Constructor<?> constructor;

        public Node(Key key, List<Key> dependencies, Constructor<?> constructor) {
            this.key = key;
            this.dependencies = dependencies;
            this.constructor = constructor;
        }

        public Key key() {
            return key;
        }

        public List<Key> dependencies() {
            return dependencies;
        }

        public Constructor<?> constructor() {
            return constructor;
        }
    }
}
