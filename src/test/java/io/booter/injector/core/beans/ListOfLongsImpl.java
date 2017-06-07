package io.booter.injector.core.beans;

import io.booter.injector.annotations.ConfiguredBy;

import java.util.ArrayList;
import java.util.Arrays;

@ConfiguredBy(LongListModule.class)
public class ListOfLongsImpl extends ArrayList<Long> implements ListOfLongs {
    public ListOfLongsImpl(Long... initial) {
        super(Arrays.asList(initial));
    }
}
