package io.booter.injector.core.beans;

import io.booter.injector.annotations.ImplementedBy;

import java.util.List;

@ImplementedBy(ListOfIntegersImpl.class)
public interface ListOfIntegers extends List<Integer> {
}
