package io.booter.injector.core.beans;

import io.booter.injector.annotations.Singleton;

import java.util.function.Consumer;

@Singleton
public class LazySingletonClassWithNotifyingConstructor {
    public LazySingletonClassWithNotifyingConstructor(Consumer<LazySingletonClassWithNotifyingConstructor> callback) {
        callback.accept(this);
    }
}
