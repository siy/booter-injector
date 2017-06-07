package io.booter.injector.core.beans;

import io.booter.injector.annotations.ConfiguredBy;

@ConfiguredBy(AnnotatedConstructorParameterClassModule.class)
public class AnnotatedConstructorParameterClass {
    private final int value;

    public AnnotatedConstructorParameterClass(@TestAnnotation int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
