package io.booter.injector.core.examples.annotated.impl;

import java.util.Arrays;
import java.util.List;

import io.booter.injector.annotations.Singleton;
import io.booter.injector.core.examples.annotated.ApplicationConfig;
import io.booter.injector.core.examples.annotated.ServiceConfig;
import io.booter.injector.core.examples.annotated.services.ListService;
import io.booter.injector.core.examples.annotated.services.SetService;

@Singleton
public class ApplicationConfigImpl implements ApplicationConfig {
    private final List<ServiceConfig> serviceConfiguration = Arrays.asList(
        ServiceConfig.of("ListService", ListService.class),
        ServiceConfig.of("SetService", SetService.class)
    );

    @Override
    public List<ServiceConfig> services() {
        return serviceConfiguration;
    }
}
