package io.booter.injector.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ChainedMap<K, V> {
    private final Map<K, V> values = new HashMap<>();
    private final ChainedMap<K, V> parent;
    private final List<ChainedMap<K, V>> children = new ArrayList<>();

    public ChainedMap() {
        this(null);
    }

    public ChainedMap(ChainedMap<K, V> parent) {
        this.parent = parent;

        if (parent != null) {
            parent.addChild(this);
        }
    }

    private void addChild(ChainedMap<K, V> child) {
        children.add(child);
    }

    public V get(K key) {
        V value = values.get(key);
        return value == null ? getFromParent(key) : value;
    }

    private V getFromParent(K key) {
        return parent == null ? null : parent.get(key);
    }

    public V put(K key, V value) {
        values.put(key, value);
        return value;
    }

    public void forEach(BiConsumer<K, V> biConsumer) {
        children.forEach(child -> child.forEach(biConsumer));
        values.forEach(biConsumer);
    }
}
