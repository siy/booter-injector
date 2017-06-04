package io.booter.injector.core.beans;

import java.util.function.Consumer;

import io.booter.injector.annotations.ComputationStyle;
import io.booter.injector.annotations.Singleton;

@Singleton(ComputationStyle.EAGER)
public class EagerSingletonClassWithNotifyingConstructor {
    public EagerSingletonClassWithNotifyingConstructor(Consumer<EagerSingletonClassWithNotifyingConstructor> callback) {
        callback.accept(this);
    }
}
