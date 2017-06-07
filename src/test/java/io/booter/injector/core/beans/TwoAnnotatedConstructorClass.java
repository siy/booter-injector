package io.booter.injector.core.beans;

import io.booter.injector.annotations.Inject;

public class TwoAnnotatedConstructorClass {
    @Inject
    public TwoAnnotatedConstructorClass(int j) {
    }

    @Inject
    public TwoAnnotatedConstructorClass(String s) {
    }
}
