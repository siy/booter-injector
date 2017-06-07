package io.booter.injector.core.beans;

import io.booter.injector.AbstractModule;
import io.booter.injector.annotations.Supplies;

public class IntegerListModule extends AbstractModule {
    @Override
    protected void configure() {
    }

    @Supplies
    public Integer[] getList() {
        return new Integer[] {82, 73, 91, 64};
    }
}
