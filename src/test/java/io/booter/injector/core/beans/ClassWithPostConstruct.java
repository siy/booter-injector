package io.booter.injector.core.beans;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

public class ClassWithPostConstruct {
    private final Consumer<Integer> consumer;

    public ClassWithPostConstruct(Consumer<Integer> consumer) {
        this.consumer = consumer;
        consumer.accept(1);
    }

    @PostConstruct
    public void postConstruct() {
        consumer.accept(2);
    }
}
