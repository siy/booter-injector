package io.booter.injector.core.examples.annotated.services;

import io.booter.injector.core.examples.annotated.Service;

public class SetService implements Service {
    @Override
    public void start() {
        System.out.println("starting " + name() + "...");
    }

    @Override
    public void stop() {
        System.out.println("stopping " + name() + "...");
    }
}
