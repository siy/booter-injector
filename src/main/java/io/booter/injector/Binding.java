package io.booter.injector;

public interface Binding<T> {
    Key key();

    T binding();

    boolean isResolved();

    boolean isSingleton();

    boolean isEager();
}
