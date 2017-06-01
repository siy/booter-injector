package io.booter.injector.core.beans;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

public class ClassWithThrowingPostConstruct {
    private final Consumer<Integer> consumer;

    public ClassWithThrowingPostConstruct(Consumer<Integer> consumer) {
        this.consumer = consumer;
        consumer.accept(1);
    }

    @PostConstruct
    public void postConstruct() {
        consumer.accept(2);
        throw new RuntimeException("Something wrong");
    }
}
