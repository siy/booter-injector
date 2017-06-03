package io.booter.injector.core.examples.annotated;

import io.booter.injector.annotations.ImplementedBy;
import io.booter.injector.core.examples.annotated.impl.ApplicationImpl;

@ImplementedBy(ApplicationImpl.class)
public interface Application {
    void run();
}
