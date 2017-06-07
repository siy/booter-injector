package io.booter.injector.core.beans;

import io.booter.injector.AbstractModule;

public class AnnotatedConstructorParameterClassModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(int.class).annotatedWith(TestAnnotation.class).toInstance(42);
    }
}
