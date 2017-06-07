package io.booter.injector.core.beans;

import io.booter.injector.AbstractModule;

public class LongListModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Long[].class).toInstance(new Long[] {91L, 82L, 73L, 64L});
    }
}
