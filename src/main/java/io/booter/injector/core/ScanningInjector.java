package io.booter.injector.core;

import io.booter.injector.Injector;
import io.booter.injector.annotations.ConfiguredBy;
import io.booter.injector.annotations.ImplementedBy;
import io.booter.injector.annotations.Supplies;
import io.booter.injector.core.exception.InjectorException;
import io.booter.injector.core.supplier.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.booter.injector.core.supplier.SupplierFactory.createInstanceSupplier;
import static io.booter.injector.core.supplier.SupplierFactory.createMethodSupplier;
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

            ChainedMap<Key, Node> map = buildTree(key, new ChainedMap<>());
//            Set<Key> configs = new HashSet<>();
            map.forEach(this::bindNode);
//            allValues(new ArrayList<>()).stream().peek(n -> {
//                if (n.isMethod()) {
//                    configs.add(n.dependencies().get(0));
//                }
//            }).
            //TODO instantiate and run configs

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

        buildTree(implKey, new ChainedMap<>()).forEach(this::bindNode);

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

            //TODO: fix it!!!
            //configure(clazz);
        }
        return this;
    }

    private <T> Injector checkForExistingBinding(Key key, boolean throwIfExists, Supplier<T> existing) {
        if (throwIfExists && existing != null) {
            throw new InjectorException("Binding for " + key + " already exists");
        }

        return this;
    }

    private void bindNode(Key key, Node node) {
        bindings.computeIfAbsent(key, (k) -> factoryLazy(() -> buildSupplier(node)));
    }

    private Supplier<?> buildSupplier(Node node) {
        return node.isMethod() ? createMethodSupplier(node.method(), collectSuppliers(node.dependencies()))
                               : createInstanceSupplier(node.constructor(), collectSuppliers(node.dependencies()));
    }

    private List<Supplier<?>> collectSuppliers(List<Key> dependencies) {
        return dependencies.stream()
                .map(this::wrapSupplier)
                .collect(Collectors.toList());
    }

    private Supplier<?> wrapSupplier(Key key) {
        return key.isSupplier() ? () -> bindings.get(key) : bindings.get(key);
    }

    private ChainedMap<Key, Node> buildTree(Key key, ChainedMap<Key, Node> nodes) {
        Node existing = nodes.get(key);
        if (existing != null) {
            if (existing.key().isSupplier() || key.isSupplier()) {
                return nodes;
            }

            throw new InjectorException("Cycle is detected for " + key);
        }

        nodes.put(key, buildNode(key, nodes)).dependencies().forEach((k) -> buildTree(k, new ChainedMap<>(nodes)));
        return nodes;
    }

    private Node buildNode(Key key, ChainedMap<Key, Node> nodes) {
        Constructor<?> constructor = findImplementationConstructor(key);

        configure(constructor, nodes);

        List<Key> dependencies = Arrays.stream(constructor.getParameters()).map(Key::of).collect(Collectors.toList());
        return new Node(key, dependencies, constructor);
    }

    private void configure(Constructor<?> constructor, ChainedMap<Key, Node> nodes) {
        ConfiguredBy configuredBy = constructor.getAnnotation(ConfiguredBy.class);

        if (configuredBy == null) {
            return;
        }

        Class<?> config = configuredBy.value();
        Key configKey = Key.of(config);

        for(Method method : config.getMethods()) {
            if (!method.isAnnotationPresent(Supplies.class)) {
                continue;
            }

            Key key = Key.of(method.getGenericReturnType(), method.getDeclaredAnnotations());
            List<Key> dependencies = new ArrayList<>();
            dependencies.add(configKey);
            dependencies.addAll(Arrays.stream(method.getParameters()).map(Key::of).collect(Collectors.toList()));
            nodes.put(key, new Node(key, dependencies, method)).dependencies().forEach((k) -> buildTree(k, new ChainedMap<>(nodes)));
        }
    }

    private Constructor<?> findImplementationConstructor(Key key) {
        if(key.rawClass().isInterface()) {
            ImplementedBy implementedBy = key.rawClass().getAnnotation(ImplementedBy.class);

            if (implementedBy != null && !implementedBy.value().isInterface()) {
                return Utils.locateConstructor(Key.of(implementedBy.value()));
            } else {
                throw new InjectorException("Unable to find suitable constructor for " + key);
            }
        }

        return Utils.locateConstructor(key);
    }

    public static class Node {
        private final Key key;
        private final List<Key> dependencies;
        private final Constructor<?> constructor;
        private final Method method;

        private Node(Key key, List<Key> dependencies, Constructor<?> constructor, Method method) {
            this.key = key;
            this.dependencies = dependencies;
            this.constructor = constructor;
            this.method = method;
        }

        public Node(Key key, List<Key> dependencies, Constructor<?> constructor) {
            this(key, dependencies, constructor, null);
        }

        public Node(Key key, List<Key> dependencies, Method method) {
            this(key, dependencies, null, method);
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

        public Method method() {
            return method;
        }

        public boolean isMethod() {
            return method != null;
        }
    }
}
