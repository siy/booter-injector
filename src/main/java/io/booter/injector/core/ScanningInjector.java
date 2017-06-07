package io.booter.injector.core;

import io.booter.injector.Injector;
import io.booter.injector.LiteInjector;
import io.booter.injector.core.supplier.SupplierFactory;
import io.booter.injector.core.supplier.Utils;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.booter.injector.core.supplier.SupplierFactory.*;
import static io.booter.injector.core.supplier.Suppliers.factoryLazy;

public class ScanningInjector implements LiteInjector {
    private final ConcurrentMap<Key, Supplier<?>> bindings = new ConcurrentHashMap<>();

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

            mergeObjectTree(key);

            return (Supplier<T>) bindings.get(key);
        }
    }

    private void mergeObjectTree(Key key) {
        Map<Key, Node> map = builtTree(key, new LinkedHashMap<>());

        map.values().forEach(n -> bindSupplier(n));
    }

    private void bindSupplier(Node node) {
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

    private Map<Key, Node> builtTree(Key key, Map<Key, Node> nodes) {
        if (nodes.containsKey(key)) {
            return nodes;
        }

        Node node = buildNode(key);
        nodes.put(key, node);

        node.dependencies().forEach((k) -> builtTree(k, nodes));

        return nodes;
    }

    private Node buildNode(Key key) {
        Constructor<?> constructor = Utils.locateConstructor(key);
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
