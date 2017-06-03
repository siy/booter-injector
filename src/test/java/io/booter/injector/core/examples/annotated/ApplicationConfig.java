package io.booter.injector.core.examples.annotated;

import java.util.List;

import io.booter.injector.annotations.ImplementedBy;
import io.booter.injector.core.examples.annotated.impl.ApplicationConfigImpl;

@ImplementedBy(ApplicationConfigImpl.class)
public interface ApplicationConfig {
    List<ServiceConfig> services();
}
