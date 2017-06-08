package io.booter.injector.core;

import io.booter.injector.Key;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

public class Node {
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
