package io.booter.injector.core.beans;

public class ClassWith1ParameterConstructor {
    private final Long p0;

    public ClassWith1ParameterConstructor(Long p0) {
        this.p0 = p0;
    }

    public Long p0() {
        return p0;
    }
}
