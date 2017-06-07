package io.booter.injector.core.beans;

import io.booter.injector.annotations.ImplementedBy;

import java.util.List;

@ImplementedBy(ListOfStringsImpl.class)
public interface ListOfStrings extends List<String> {
}
