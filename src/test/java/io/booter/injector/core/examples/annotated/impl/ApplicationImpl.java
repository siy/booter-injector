package io.booter.injector.core.examples.annotated.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.booter.injector.annotations.Singleton;
import io.booter.injector.core.examples.annotated.Application;
import io.booter.injector.core.examples.annotated.ApplicationConfig;
import io.booter.injector.core.examples.annotated.Service;
import io.booter.injector.core.examples.annotated.ServiceFactory;

@Singleton
public class ApplicationImpl implements Application {
    private static final long ONE_DOT_DURATION = 200;
    private static final int DOT_COUNT = 5;

    private final ApplicationConfig config;
    private final ServiceFactory factory;

    public ApplicationImpl(ApplicationConfig config, ServiceFactory factory) {
        this.config = config;
        this.factory = factory;
    }

    @Override
    public void run() {
        List<Service> running = startServices();

        waitForEvent();

        stopServices(running);
    }

    private void stopServices(List<Service> running) {
        running.stream()
               .peek(Service::stop)
               .forEach(service -> log("Stopped " + service.name()));
    }

    private List<Service> startServices() {
        log("Application is starting...");
        List<Service> running = config.services().stream()
                                      .map(factory::create)
                                      .peek(Service::start)
                                      .peek(service -> log("Started " + service.name()))
                                      .collect(Collectors.toList());
        Collections.reverse(running);
        return running;
    }

    private void waitForEvent() {
        //In real application here can be waiting for some event, but in this example
        //we just wait for few seconds and then stop

        log("Application is running");
        for (int i = 0; i < DOT_COUNT; i++) {
            sleep(ONE_DOT_DURATION);
            log(".");
        }
        log("Application is stopping");
    }

    private void log(String message) {
        System.out.println(message);
    }

    private static void sleep(long durationInMs) {
        try {
            Thread.sleep(durationInMs);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
