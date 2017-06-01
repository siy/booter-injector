package io.booter.injector.core.beans;

import io.booter.injector.annotations.ComputationStyle;
import io.booter.injector.annotations.Singleton;

import java.util.function.Consumer;

@Singleton(value = ComputationStyle.EAGER)
public class EagerSingletonClassWithNotifyingConstructor {
    public EagerSingletonClassWithNotifyingConstructor(Consumer<EagerSingletonClassWithNotifyingConstructor> callback) {
        callback.accept(this);
    }
}
