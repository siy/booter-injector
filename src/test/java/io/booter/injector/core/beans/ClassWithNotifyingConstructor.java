package io.booter.injector.core.beans;

import java.util.function.Consumer;

public class ClassWithNotifyingConstructor {
    public ClassWithNotifyingConstructor(Consumer<ClassWithNotifyingConstructor> callback) {
        callback.accept(this);
    }
}
