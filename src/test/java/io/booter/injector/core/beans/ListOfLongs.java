package io.booter.injector.core.beans;

import io.booter.injector.annotations.ImplementedBy;

import java.util.List;

@ImplementedBy(ListOfLongsImpl.class)
public interface ListOfLongs extends List<Long> {
}
