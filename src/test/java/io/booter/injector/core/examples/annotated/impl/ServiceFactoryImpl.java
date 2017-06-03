package io.booter.injector.core.examples.annotated.impl;

import io.booter.injector.Injector;
import io.booter.injector.annotations.Singleton;
import io.booter.injector.core.examples.annotated.Service;
import io.booter.injector.core.examples.annotated.ServiceConfig;
import io.booter.injector.core.examples.annotated.ServiceFactory;

@Singleton
public class ServiceFactoryImpl implements ServiceFactory {
    private final Injector injector;

    public ServiceFactoryImpl(Injector injector) {
        this.injector = injector;
    }

    @Override
    public Service create(ServiceConfig config) {
        return injector.get(config.implementation());
    }
}
