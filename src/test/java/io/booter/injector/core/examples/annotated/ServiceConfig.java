package io.booter.injector.core.examples.annotated;

public class ServiceConfig {
    private final String name;
    private final Class<? extends Service> implementation;

    private ServiceConfig(String name, Class<? extends Service> implementation) {
        this.name = name;
        this.implementation = implementation;
    }

    public static ServiceConfig of(String name, Class<? extends Service> implementation) {
        return new ServiceConfig(name, implementation);
    }

    public String name() {
        return name;
    }

    public Class<? extends Service> implementation() {
        return implementation;
    }
}
