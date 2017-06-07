package io.booter.injector.core.beans;

import io.booter.injector.annotations.BindingAnnotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@Documented
@BindingAnnotation
public @interface TestAnnotation {
}
