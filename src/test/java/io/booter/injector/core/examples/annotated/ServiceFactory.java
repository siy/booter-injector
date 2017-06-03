package io.booter.injector.core.examples.annotated;

import io.booter.injector.annotations.ImplementedBy;
import io.booter.injector.core.examples.annotated.impl.ServiceFactoryImpl;

@ImplementedBy(ServiceFactoryImpl.class)
public interface ServiceFactory {
    Service create(ServiceConfig config);
}
