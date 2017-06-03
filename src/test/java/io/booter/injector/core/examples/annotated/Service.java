package io.booter.injector.core.examples.annotated;

public interface Service {
    void start();
    void stop();

    default String name() {
        return getClass().getSimpleName();
    }
}
