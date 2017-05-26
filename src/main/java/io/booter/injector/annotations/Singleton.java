package io.booter.injector.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface Singleton {
    ComputationStyle value() default ComputationStyle.LAZY;
}
