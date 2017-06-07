package io.booter.injector.core.beans;

public class AnnotatedConstructorParameterClassWithoutConfiguredBy {
    private final int value;

    public AnnotatedConstructorParameterClassWithoutConfiguredBy(@TestAnnotation int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
