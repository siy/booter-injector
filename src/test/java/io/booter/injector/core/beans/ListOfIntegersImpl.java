package io.booter.injector.core.beans;

import io.booter.injector.annotations.ConfiguredBy;

import java.util.ArrayList;
import java.util.Arrays;

@ConfiguredBy(IntegerListModule.class)
public class ListOfIntegersImpl extends ArrayList<Integer> implements ListOfIntegers {
    public ListOfIntegersImpl(Integer[] initial) {
        super(Arrays.asList(initial));
    }
}
